IDS=$(sudo docker ps -a | grep sdk-example-benchmark | awk '{print $1}')
sudo docker stop $IDS