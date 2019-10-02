package com.saucelabs.simplesauce.unit;

import com.saucelabs.simplesauce.DataCenter;
import org.hamcrest.text.IsEqualIgnoringCase;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class DataCenterTest
{
    @Test
    public void usWestDataCenterUrl_isCorrect()
    {
        assertThat(DataCenter.RemoteUrls.US_WEST.address,
                IsEqualIgnoringCase.equalToIgnoringCase("https://ondemand.saucelabs.com/wd/hub"));
    }
}
