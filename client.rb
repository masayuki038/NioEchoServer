# -*- coding: utf-8 -*-

require 'socket'

s = Socket.new(Socket::AF_INET, Socket::SOCK_STREAM, 0)
sockaddr = Socket.sockaddr_in(8000, '127.0.0.1')
s.connect(sockaddr)
sleep 5
s.write "Hello,"
puts "Received #{s.recv(1024)}"
sleep 5
s.write "world."
puts "Received #{s.recv(1024)}"

s.close
