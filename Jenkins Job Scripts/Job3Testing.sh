#!/bin/bash
WebPort=$(kubectl get service -n webapp -o=jsonpath="{.items[0].spec.ports[0].nodePort}")
echo $WebPort
status=$(curl -s -o /dev/null -I -w "%{http_code}" 172.16.16.100:$WebPort)
if [[ $status == 200 ]]; then  echo "good"; else exit 1 ; fi