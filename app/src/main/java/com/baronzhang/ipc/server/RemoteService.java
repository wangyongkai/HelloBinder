package com.baronzhang.ipc.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import com.baronzhang.ipc.Book;

import java.util.ArrayList;
import java.util.List;

public class RemoteService extends Service {

    private List<Book> books = new ArrayList<>();

    public RemoteService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("RemoteService", " onCreate-----PID=" + Process.myPid());
        Book book = new Book();
        book.setName("三体");
        book.setPrice(88);
        books.add(book);
    }

    @Override
    public IBinder onBind(Intent intent) {


        Log.d("RemoteService", " onBind-----PID=" + Process.myPid());


        return bookManager;
    }

    private final Stub bookManager = new Stub() {//这个对象就是要给client用的！ 但是跨进程穿不过去 需要代理
        @Override
        public List<Book> getBooks() throws RemoteException {

            Log.d("RemoteService", " getBooks-----PID=" + Process.myPid());

            synchronized (this) {
                if (books != null) {
                    return books;
                }
                return new ArrayList<>();
            }
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            synchronized (this) {
                if (books == null) {
                    books = new ArrayList<>();
                }

                if (book == null)
                    return;

                book.setPrice(book.getPrice() * 2);
                books.add(book);
                Log.d("RemoteService", " getBooks-----PID=" + Process.myPid() + " books: " + book.toString());
                // Log.e("Server", "books: " + book.toString());
            }
        }
    };
}
