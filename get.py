#!/usr/bin/env python

import os

for num in ["01","02","03","04","09","10","11","13","50"]:
    day = num + "d.png"
    night = num + "n.png"
    urld = "http://openweathermap.org/img/w/" + day
    urln = "http://openweathermap.org/img/w/" + night 
    cmd1 = "wget "+urld
    cmd2 = "wget "+urln
    os.system(cmd1)
    os.system(cmd2)

