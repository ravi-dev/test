// kibo-admin
freeStyleJob("${FolderNAme}/kibo-admin") {        
    label('Jenkins-EC2-Slave')
    logRotator {
	    numToKeep(3)
	    daysToKeep(3)
	    }
    scm {
        git {
            remote {
            credentials('bkona-alopa')
            url("https://github.com/sana-rx/kibo_admin.git")
            }
            branch('master')
        }
    } 
    triggers {
        githubPush()
    }
    wrappers {
        preBuildCleanup()
        credentialsBinding {
        usernamePassword {
        // Name of an environment variable to be set to the username during the build.
        usernameVariable('GIT_USRNAME')
        // Name of an environment variable to be set to the password during the build.
        passwordVariable('GIT_PASWD')
        // Credentials of an appropriate type to be set to the variable.
        credentialsId('bkona-alopa')
        }
    }
    }
    steps {
        shell("""docker build --build-arg gitusername=\${GIT_USRNAME}  --build-arg gitpassword=\${GIT_PASWD} -t kibo-admin:latest .
docker save --output kibo-admin.tar kibo-admin:latest""")
    }
    publishers {
        archiveArtifacts("kibo-admin.tar")
        extendedEmail {
            recipientList('bks@amalgarx.com')
            replyToList('bks@amalgamrx.com')
            defaultSubject("\${DEFAULT_SUBJECT}")
            defaultContent("\${DEFAULT_CONTENT}")
            contentType('default')
            attachBuildLog(false)
            triggers {
                always {
                    sendTo {
                        recipientList()
                    }
                }
            }
        }
    }
    properties{
        copyArtifactPermission {
            // Comma-separated list of projects that can copy artifacts of this project.
            projectNames('kibo-deployer')
        }
		promotions{
			promotion {
                name('Deploy_to_DEV')
                icon('star-gold')
                conditions {
                    manual('bkona@alopasoft.com,aharshetty@alopasoft.com,asuhail@alopasoft.com')
                }
                actions {
                    downstreamParameterized {
                        trigger('kibo-deployer') {
                            block {
                                buildStepFailure('FAILURE')
                                failure('FAILURE')
                                unstable('UNSTABLE')
                            }
                            parameters {
                                predefinedProp("JobName", "\$PROMOTED_JOB_NAME")
                                predefinedProp("BUILDNO", "\$PROMOTED_NUMBER")
                                predefinedProp("ENVI", "DEV")
                            }
                        }
                    }
                }
		    }
	    }
    }
}
