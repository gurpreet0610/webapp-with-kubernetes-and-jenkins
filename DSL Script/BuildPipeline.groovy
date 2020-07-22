job("GithubPull"){
    scm {
    github('gurpreet0610/webapp-with-kubernetes-and-jenkins')
    }
    
    triggers {
        githubPush()
    }
    steps {
        shell("pwd")
      }

}

job("KubernetesDeploy"){
    triggers{
		upstream('GithubPull' , 'SUCCESS')
	}
    steps {
        shell('''cd /var/jenkins_home/workspace/GithubPull
kubectl apply -k . -n webapp
kubectl get all -n webapp '''.trim())
      }
}


job("Testing"){
    triggers{
		upstream('KubernetesDeploy' , 'SUCCESS')
	}
    steps {
        shell('''#!/bin/bash
WebPort=$(kubectl get service -n webapp -o=jsonpath="{.items[0].spec.ports[0].nodePort}")
echo $WebPort
status=$(curl -s -o /dev/null -I -w "%{http_code}" 172.16.16.100:$WebPort)
if [[ $status == 200 ]]; then  echo "good"; else exit 1 ; fi
        '''.trim())
      }
    publishers {
            extendedEmail {
                recipientList('gurpreets0610@gmail.com')
                defaultSubject("Jenkins Job started : ${JOB_NAME}")
                defaultContent("See the latest build in the jenkins job here http://0.0.0.0:8080/job/${JOB_NAME}/ <pre> \${BUILD_LOG, maxLines=30, escapeHtml=false} </pre>")
                attachBuildLog(true)
                contentType('text/html')
                triggers {
                    failure {
                        content("See the latest build in the jenkins job here http://0.0.0.0:8080/job/${JOB_NAME}/ <pre> \${BUILD_LOG, maxLines=30, escapeHtml=false} </pre>")
                        contentType('text/html')
                        recipientList('gurpreets0610@gmail.com')
                        subject("Build Failed in Jenkins: ${JOB_NAME}")
                    }
                    success {
                        content('See the latest build in the jenkins job here http://0.0.0.0:8080/job/\${JOB_NAME}/ <pre> \${BUILD_LOG, maxLines=30, escapeHtml=false} </pre>')
                        contentType('text/html')
                        recipientList('gurpreets0610@gmail.com')
                        subject("Build Success in Jenkins: ${JOB_NAME}")
                    }
                }
            }
        }
}

job("RollBack"){
    triggers{
		upstream('Testing' , 'FAILURE')
	}
    steps {
        shell('''cd /var/jenkins_home/workspace/GithubPull
git reset --hard HEAD@{1}
kubectl apply -k . -n webapp
            '''.trim())
      }
   
buildPipelineView('pipe-view') {
  filterBuildQueue(true)
  filterExecutors(false)
  title('KubeWeb')
  displayedBuilds(1)
  selectedJob('githubpull')
  alwaysAllowManualTrigger(false)
  showPipelineParameters(true)
  refreshFrequency(1) }
}
