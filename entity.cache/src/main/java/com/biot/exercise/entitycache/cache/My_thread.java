package com.biot.exercise.entitycache.cache;

class My_thread extends Thread{

    private int field_1 = 0;
    private int field_2 = 0;
    
    public void run()
    {
       // setDaemon(true); // this thread will not keep the app alive
    
        while( true )
        {
            System.out.println( Thread.currentThread().getName() +" field_1=" + field_1 + " field_2=" + field_2 );
            try {
				sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
    
    synchronized public void modify( int new_value )
    { 
    	// System.out.println( Thread.currentThread().getName() +" in modify, value=" + new_value);
        field_1 = new_value;
        field_2 = new_value;
    }

	
	// Driver method
	public static void main(String[] args)
	{
		My_thread test = new My_thread();
        test.start();
        test.modify(1);
        
        test.modify(5);
        
       
        new Thread(new Runnable() {

				@Override
				public void run() {
					int i = 8;
					while( true )
					 test.modify(i++);
					
				} 
	        	
	        }
        , "sss").start();
        
	}
}