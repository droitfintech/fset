pipelineJob('non-prod/fset/release') {
  logRotator {
    daysToKeep(10)
    numToKeep(10)
  }
  triggers {
    scm('*/1 * * * *')
  }
  definition {
    cpsScm {
      scm {
        git {
          remote {
            github('droitfintech/fset', 'ssh')
            credentials('320f42f1-7c12-4a5d-9bbe-0b7f9b022a77')
            refspec('+refs/heads/main:refs/remotes/origin/main')
          }
          branch('*/main')
        }
      }
      scriptPath('.jenkins/pipelines/Jenkinsfile-build')
    }
  }
}
