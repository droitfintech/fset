pipelineJob('non-prod/fset/pr') {
  logRotator {
    daysToKeep(15)
    numToKeep(15)
  }
  triggers {
    githubPullRequest {
      orgWhitelist('droitfintech')
      cron('*/3 * * * *')
      triggerPhrase('OK to test')
      allowMembersOfWhitelistedOrgsAsAdmin()
      extensions {
        commitStatus {
          context('jenkins')
        }
      }
    }
  }
  definition {
    cpsScm {
      scm {
        git {
          remote {
            github('droitfintech/fset', 'ssh')
            credentials('320f42f1-7c12-4a5d-9bbe-0b7f9b022a77')
            refspec('+refs/pull/*:refs/remotes/origin/pr/*')
          }
          branch('${sha1}')
        }
      }
      scriptPath('.jenkins/pipelines/Jenkinsfile')
    }
  }
}
