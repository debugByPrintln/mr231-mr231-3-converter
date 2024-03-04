import javafx.scene.chart.ScatterChart;
import org.example.searadar.mr231_3.convert.Mr231_3Converter;
import org.example.searadar.mr231_3.station.Mr231_3StationType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ru.oogis.searadar.api.message.InvalidMessage;
import ru.oogis.searadar.api.message.RadarSystemDataMessage;
import ru.oogis.searadar.api.message.SearadarStationMessage;
import ru.oogis.searadar.api.message.TrackedTargetMessage;

import java.util.List;

public class TestMr231_3Converter {
    Mr231_3StationType mr231_3 = new Mr231_3StationType();
    Mr231_3Converter mr231_3Converter = mr231_3.createConverter();

    String correctTTM = "$RATTM,23,13.88,137.2,T,63.8,094.3,T,9.2,79.4,N,b,T,,783344,А*42";
    String correctRSD = "$RARSD,36.5,331.4,8.4,320.6,,,,,11.6,185.3,96.0,N,N,S*33";

    String wrongMessage = "May the force be with you!";
    String wrongTargetNumberTTM = "$RATTM,66,13.88,137.2,T,63.8,094.3,T,9.2,79.4,N,b,T,,783344,А*42";
    String wrongDistanceTTM = "$RATTM,23,33.88,137.2,T,63.8,094.3,T,9.2,79.4,N,b,T,,783344,А*42";
    String wrongBearingTTM = "$RATTM,23,13.88,400.2,T,63.8,094.3,T,9.2,79.4,N,b,T,,783344,А*42";
    String wrongSpeedTTM = "$RATTM,23,13.88,137.2,T,99.8,094.3,T,9.2,79.4,N,b,T,,783344,А*42";
    String wrongCourseTTM = "$RATTM,23,13.88,137.2,T,63.8,400.3,T,9.2,79.4,N,b,T,,783344,А*42";
    String wrongDistanceScaleRSD = "$RARSD,36.5,331.4,8.4,320.6,,,,,11.6,185.3,95.0,N,N,S*33";

    @Test
    public void testCorrectTTM(){
        List<SearadarStationMessage> stationMessages = mr231_3Converter.convert(correctTTM);
        assertTrue(stationMessages.get(0) instanceof TrackedTargetMessage, "Wrong message type! Expected TrackedTargetMessage but get " + stationMessages.get(0).getClass());

        TrackedTargetMessage ttm = (TrackedTargetMessage) stationMessages.get(0);
        assertEquals(23, ttm.getTargetNumber(), "Wrong target number!");
        assertEquals(13.88, ttm.getDistance(), "Wrong distance!");
        assertEquals(137.2, ttm.getBearing(), "Wrong bearing!");
        assertEquals(94.3, ttm.getCourse(), "Wrong course!");
        assertEquals(63.8, ttm.getSpeed(), "Wrong speed!");
        assertEquals("UNKNOWN", ttm.getType().name(), "Wrong type!");
        assertEquals("TRACKED", ttm.getStatus().name(), "Wrong status!");
        assertEquals("FRIEND", ttm.getIff().name(), "Wrong iff!");
    }

    @Test
    public void testCorrectRSD(){
        List<SearadarStationMessage> stationMessages = mr231_3Converter.convert(correctRSD);
        assertTrue(stationMessages.get(0) instanceof RadarSystemDataMessage, "Wrong message type! Expected RadarSystemDataMessage but get " + stationMessages.get(0).getClass());

        RadarSystemDataMessage rsd = (RadarSystemDataMessage) stationMessages.get(0);
        assertEquals(36.5, rsd.getInitialDistance(), "Wrong initial distance!");
        assertEquals(331.4, rsd.getInitialBearing(), "Wrong initial bearing!");
        assertEquals(8.4, rsd.getMovingCircleOfDistance(), "Wrong moving circle of distance!");
        assertEquals(320.6, rsd.getBearing(), "Wrong bearing!");
        assertEquals(11.6, rsd.getDistanceFromShip(), "Wrong distance from ship!");
        assertEquals(185.3, rsd.getBearing2(), "Wrong bearing 2!");
        assertEquals(96.0, rsd.getDistanceScale(), "Wrong distance scale!");
        assertEquals("N", rsd.getDistanceUnit(), "Wrong distance unit!");
        assertEquals("N", rsd.getDisplayOrientation(), "Wrong display orientation!");
        assertEquals("S", rsd.getWorkingMode(), "Wrong working mode!");
    }

    @Test
    public void testWrongMessage(){
        assertThrows(StringIndexOutOfBoundsException.class, () -> {
           mr231_3Converter.convert(wrongMessage);
        }, "Converter must throw StringIndexOutOfBoundsException!");
    }

    @Test
    public void testWrongTargetNumberTTM(){
        List<SearadarStationMessage> stationMessages = mr231_3Converter.convert(wrongTargetNumberTTM);
        assertTrue(stationMessages.get(0) instanceof InvalidMessage, "Wrong message type! Expected InvalidMessage but get " + stationMessages.get(0).getClass());

        InvalidMessage invalidMessage = (InvalidMessage) stationMessages.get(0);
        assertEquals("TTM message. Wrong distance (01-50): 66", invalidMessage.getInfoMsg(), "Wrong invalid message!");
    }

    @Test
    public void testWrongDistanceTTM(){
        List<SearadarStationMessage> stationMessages = mr231_3Converter.convert(wrongDistanceTTM);
        assertTrue(stationMessages.get(0) instanceof InvalidMessage, "Wrong message type! Expected InvalidMessage but get " + stationMessages.get(0).getClass());

        InvalidMessage invalidMessage = (InvalidMessage) stationMessages.get(0);
        assertEquals("TTM message. Wrong distance (0.0-32.0): 33.88", invalidMessage.getInfoMsg(), "Wrong invalid message!");
    }

    @Test
    public void testWrongBearingTTM(){
        List<SearadarStationMessage> stationMessages = mr231_3Converter.convert(wrongBearingTTM);
        assertTrue(stationMessages.get(0) instanceof InvalidMessage, "Wrong message type! Expected InvalidMessage but get " + stationMessages.get(0).getClass());

        InvalidMessage invalidMessage = (InvalidMessage) stationMessages.get(0);
        assertEquals("TTM message. Wrong bearing (0.0-359.9): 400.2", invalidMessage.getInfoMsg(), "Wrong invalid message!");
    }

    @Test
    public void testWrongSpeedTTM(){
        List<SearadarStationMessage> stationMessages = mr231_3Converter.convert(wrongSpeedTTM);
        assertTrue(stationMessages.get(0) instanceof InvalidMessage, "Wrong message type! Expected InvalidMessage but get " + stationMessages.get(0).getClass());

        InvalidMessage invalidMessage = (InvalidMessage) stationMessages.get(0);
        assertEquals("TTM message. Wrong speed (0.0-90.0): 99.8", invalidMessage.getInfoMsg(), "Wrong invalid message!");
    }

    @Test
    public void testWrongCourseTTM(){
        List<SearadarStationMessage> stationMessages = mr231_3Converter.convert(wrongCourseTTM);
        assertTrue(stationMessages.get(0) instanceof InvalidMessage, "Wrong message type! Expected InvalidMessage but get " + stationMessages.get(0).getClass());

        InvalidMessage invalidMessage = (InvalidMessage) stationMessages.get(0);
        assertEquals("TTM message. Wrong course (0.0-359.9): 400.3", invalidMessage.getInfoMsg(), "Wrong invalid message!");
    }

    @Test
    public void testWrongDistanceScaleRSD(){
        List<SearadarStationMessage> stationMessages = mr231_3Converter.convert(wrongDistanceScaleRSD);
        assertTrue(stationMessages.get(0) instanceof InvalidMessage, "Wrong message type! Expected InvalidMessage but get " + stationMessages.get(0).getClass());

        InvalidMessage invalidMessage = (InvalidMessage) stationMessages.get(0);
        assertEquals("RSD message. Wrong distance scale value: 95.0", invalidMessage.getInfoMsg(), "Wrong invalid message!");
    }
}
