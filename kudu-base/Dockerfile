FROM debian:jessie

RUN apt-get update && apt-get -y --no-install-recommends install wget curl && \
  cd /etc/apt/sources.list.d && \
  wget -qO - http://archive.cloudera.com/kudu/debian/jessie/amd64/kudu/archive.key | apt-key add - && \
  wget http://archive.cloudera.com/kudu/debian/jessie/amd64/kudu/cloudera.list && \
  apt-get update && \
  apt-get -y --no-install-recommends install kudu kudu-master kudu-tserver libkuduclient0 libkuduclient-dev

VOLUME /var/lib/kudu/master /var/lib/kudu/tserver

ADD entrypoint.sh /
RUN chmod a+x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
EXPOSE 8050 8051 7050 7051
CMD ["help"]
