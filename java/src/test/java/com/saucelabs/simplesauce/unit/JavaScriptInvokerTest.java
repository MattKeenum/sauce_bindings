package com.saucelabs.simplesauce.unit;

import com.saucelabs.simplesauce.JavaScriptInvoker;
import com.saucelabs.simplesauce.JavaScriptInvokerFactory;
import com.saucelabs.simplesauce.JavaScriptInvokerImpl;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.*;


public class JavaScriptInvokerTest extends BaseUnitTest
{
    @Test
    public void shouldExecuteScriptOneTimeWhenMockManagerIsSet()
    {
        JavaScriptInvokerImpl mockJsManager = mock(JavaScriptInvokerImpl.class);

        JavaScriptInvokerFactory.setJavaScriptExecutor(mockJsManager);
        JavaScriptInvoker js = new JavaScriptInvoker(mockWebDriver);
        js.executeScript("test");
        verify(mockJsManager, times(1)).executeScript("test");
    }

    @Test
    public void shouldReturnJavaScriptInvokerWhenManagerNotSet()
    {
        JavaScriptInvoker js = new JavaScriptInvoker(mockWebDriver);
        assertThat(js, instanceOf(JavaScriptInvoker.class));
    }

}
