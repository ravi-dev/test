Binding binding = new Binding();
freeStyleJob('DSL-JOBS/kibo-admin-dsl-test') {
    description('testing job dsl on kibo-admin')  
    label('mac-slave')
    logRotator {
	    artifactNumToKeep(3)
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
    }
    steps {
        shell('docker build -t kibo-admin:latest .')
        shell('docker save --output kibo-admin.tar kibo-admin:latest')
    }
    publishers {
        archiveArtifacts("*.tar")
        extendedEmail {
            recipientList('bks@amalgarx.com')
            defaultSubject("\${DEFAULT_SUBJECT}")
            defaultContent("\${DEFAULT_CONTENT}")
            contentType('text/html')
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
		promotions{
			promotion {
                name('Deploy_to_DEV')
                icon('star-gold')
                conditions {
                    manual('balamani')
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
                                predefinedProp("JobName", "\${PROMOTED_JOB_NAME}")
                                predefinedProp("BUILDNO", "\${PROMOTED_NUMBER}")
                                predefinedProp("ENVI", "DEV")
                            }
                        }
                    }
                }
		    }
	    }
    }
}
