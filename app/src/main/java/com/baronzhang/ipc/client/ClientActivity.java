package com.baronzhang.ipc.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.baronzhang.ipc.Book;
import com.baronzhang.ipc.R;
import com.baronzhang.ipc.server.BookManager;
import com.baronzhang.ipc.server.RemoteService;
import com.baronzhang.ipc.server.Stub;

import java.util.List;

public class ClientActivity extends AppCompatActivity {


//----------------------------------------------------------------------------

    //https://zhuanlan.zhihu.com/p/35519585

//为什要跨进程通信？
//因为linux两个不同的进程属于不同的用户空间。用户空间之间不能直接通信，必须经过中间的内核空间。

//为什么不用传统的ipc机制 ?
//传统的ipc是在内核中开辟一块缓存空间。用户1--缓存空间--用户2.这样数据必须copy两次才能从1到2。

//binder用到了什么技术？
//1.动态内核可加载模块 Android 系统就可以通过动态添加一个内核模块运行在内核空间.这个binder的内核模块就叫 Binder 驱动（Binder Dirver）。
//2.内存映射  内存映射简单的讲就是将用户空间的一块内存区域映射到内核空间。映射关系建立后，用户对这块内存区域的修改可以直接反应到内核空间；
// 反之内核空间对这段区域的修改也能直接反应到用户空间。

//----------------------------------------------------------------------------

//binder实现中的三个重要角色：
    //ServiceManager是单独跑在一个叫system_manager进程中的，是用来管理服务和查询服务的
    //client是一个进程 activity所在进程
    //server service所在的进程


    //通信的主要过程://
    // 1.server创建binder并起名xxx
    // 2.server向ServiceManager注册这个binder(跨进程咋注册 因为ServiceManager中自带另一个binder 并对外公布这个binder的0号引用)
    // 3.binder驱动将server中的binder在内核中创建并给ServiceManager这个binder的引用（这个应该也发生在2中的注册步骤）
    // 4.client通过ServiceManager的0号引用向ServiceManager请求binder的引用 拿到引用后就相当于建联

    //binder引用是个啥？可以理解为在内核空间的地址。用户空间与内核空间是通的。

//----------------------------------------------------------------------------
    //问题：代理是什么意思？
    //对象在进程间不能传递，需要中间代理来实现。
    //onServiceConnected方法返回的是此处的service类型是android.os.BinderProxy 代理





//----------------------------------------------------------------------------
//----------------------------------------------------------------------------
//----------------------------------------------------------------------------
//----------------------------------------------------------------------------
//----------------------------------------------------------------------------


    private BookManager bookManager;
    private boolean isConnection = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        Button btn = findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isConnection) {
                    attemptToBindService();
                    return;
                }

                if (bookManager == null)
                    return;

                try {
                    Book book = new Book();
                    book.setPrice(101);
                    book.setName("编码");
                    bookManager.addBook(book);

                    Log.d("ClientActivity", bookManager.getBooks().toString());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void attemptToBindService() {

        Intent intent = new Intent(this, RemoteService.class);
        intent.setAction("com.baronzhang.ipc.server");
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isConnection = true;
            bookManager = Stub.asInterface(service);//此处的service类型是android.os.BinderProxy
            if (bookManager != null) {
                try {
                    List<Book> books = bookManager.getBooks();
                    Log.d("ClientActivity", books.toString());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isConnection = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (!isConnection) {
            attemptToBindService();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isConnection) {
            unbindService(serviceConnection);
        }
    }
}
