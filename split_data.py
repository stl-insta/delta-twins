#!usr/bin/python3

import sys

if __name__ == "__main__":
    # split_data.py file timeSpan
    args = sys.argv[1:]

    # Not enough arguments
    if len(args) < 2:
        print("argument(s) is/are missing", file=sys.stderr)
        exit(0)

    # Getting file informations --> name, extension
    file_infos = args[0].split(".")
    filename = file_infos[0]
    file_extension = file_infos[1]

    # Getting time range argument in seconds
    time_range = int(args[1])

    file = open(args[0], 'r')

    first_line = file.readline()
    first_line_split = first_line.split(" ")

    separator = int(first_line_split[len(first_line_split) - 1]) - time_range

    count = 1
    new_file = open(filename + '_' + str(count) + "." + file_extension, 'w')
    new_file.write(first_line)
    for line in file:
        l = line.split(" ")
        if int(l[len(l) - 1]) < separator:
            separator -= time_range
            count += 1
            new_file = open(filename + '_' + str(count) + "." + file_extension, 'w')

        new_file.write(line)