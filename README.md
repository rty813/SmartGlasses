# SmartGlasses
![pic]("https://hackster.imgix.net/uploads/attachments/282489/2744261482922947650.jpg")

这个项目是老司机于2017-05-14日在[hackster]("https://www.hackster.io/alain-mauer/arduino-glasses-hmd-for-multimeter-b6cead")上面发现的一个项目，是个智能眼镜。下面是他的介绍：

>**Description**
>
>Trying to build a cheap Arduino Data Glasses for everybody.
>
>It's working, and now it can even help to avoid accidents. From the first idea to the working prototype, it took 4 Month
>
>The challenge was, that It should be constructed out of common materials that can be found easily. The project is more about how to build the optical system for this HMD I have a few ideas and one of them is to connect it to a Multimeter over Bluetooth to get the Data right in front of my eyes. One thing is clear, you can not just place a screen in front of your eyes, because it will not be possible for them to focus it.
>
>In the logs you can see from the first tests to what I have now. The whole project runs under Open Source License.

老司机打算用这个玩意参加中美创客，让我做一个手机App客户端，来通过串口发送短信、时间、qq、微信的通知。主要的技术在于，这里用了一个以前没用过的service，叫做NotificationListenerService，用来监听通知。在这个项目里还广泛运用了BroadCast，加深了理解。