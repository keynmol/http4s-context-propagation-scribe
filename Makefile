server-example:
	scala-cli package . -f -M ServerExample -o server-example

basic-example:
	scala-cli package . -f -M BasicExample -o basic-example

server-client-example:
	scala-cli package . -f -M ServerClientExample -o server-client-example

clean:
	rm -f ./server-example ./basic-example ./server-client-example
