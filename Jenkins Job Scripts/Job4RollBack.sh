cd /var/jenkins_home/workspace/Job1_GithubPull
git reset --hard HEAD@{1}
kubectl apply -k . -n webapp