# CentralizedPriorityScheduler

In case of a centralized server with multiple client the server has to decide which clients request to execute first.

Here is implementation for such a case using priority based premptive scheduling algorithm.

The server considers the priority of the requesting client to decide whether to place the request in the queue or prempt the current execution.

For demo purpose the program excepts the execution command to be of the format (burst_time priority) (without brackets.!)
