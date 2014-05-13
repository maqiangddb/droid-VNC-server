;rem upload guac
@echo adb push guac /system/xbin/
adb push guac /system/xbin/
adb akshell chmod 775 /system/xbin/guac

;rem upload start_guac.sh
@echo adb push guac.in /system/bin/
adb push guac.in /system/bin/
adb akshell chmod 775 /system/bin/guac.in

@echo 更新完毕,按任意键重启设备
@pause