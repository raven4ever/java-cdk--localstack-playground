yum update -y
yum install -y httpd

systemctl start httpd
systemctl enable httpd

EC2_AVAIL_ZONE=`curl -v http://169.254.169.254/latest/meta-data/placement/availability-zone`

echo "<h1>Hello beautiful world from $(hostname –f) in AZ $EC2_AVAIL_ZONE </h1>." > /var/www/html/index.html
