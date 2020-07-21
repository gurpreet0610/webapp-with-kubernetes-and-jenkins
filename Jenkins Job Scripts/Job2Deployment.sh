cd /var/jenkins_home/workspace/Job1_GithubPull
kubectl apply -k . -n webapp
kubectl get all -n webapp