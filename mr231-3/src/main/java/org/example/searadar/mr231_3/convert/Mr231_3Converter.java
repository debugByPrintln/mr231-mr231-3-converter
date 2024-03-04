package org.example.searadar.mr231_3.convert;

import org.apache.camel.Exchange;
import ru.oogis.searadar.api.convert.SearadarExchangeConverter;
import ru.oogis.searadar.api.message.*;
import ru.oogis.searadar.api.types.IFF;
import ru.oogis.searadar.api.types.TargetStatus;
import ru.oogis.searadar.api.types.TargetType;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Данный класс является парсером для обработки сообщений формата ТТМ и RSD (по стандарту IEC61162-1:2000 (NMEA-0183))
 * для для получения информации от НРЛС МР-231-3.
 * <p>Пример сообщения формата TTM:
 * $RATTM,23,13.88,137.2,T,63.8,094.3,T,9.2,79.4,N,b,T,,783344,А*42
 * Пример сообщения формата RSD:
 * $RARSD,36.5,331.4,8.4,320.6,,,,,11.6,185.3,96.0,N,N,S*33</p>
 * <p>Парсер работает аналогично парсеру для НРЛС МР-231. {@link org.example.searadar.mr231.convert.Mr231Converter}</p>
 * <p>Класс реализует интерфейс SearadarExchangeConverter. {@link ru.oogis.searadar.api.convert.SearadarExchangeConverter}</p>
 */
public class Mr231_3Converter implements SearadarExchangeConverter {
    /**
     * Допустимые значения шкалы дальности для RSD сообщений.
     */
    private static final Double[] DISTANCE_SCALE = {0.125, 0.25, 0.5, 1.5, 3.0, 6.0, 12.0, 24.0, 48.0, 96.0};

    /**
     * Массив, содержащий поля TTM или RSD сообщения.
     */
    private String[] fields;

    /**
     * Тип обрабатываемого сообщения.
     */
    private String msgType;

    /**
     * Преобразует входную информацию обмена в список объектов типа SearadarStationMessage.
     *
     * @param exchange Объект Exchange, содержащий информацию для преобразования.
     * @return Список объектов типа SearadarStationMessage, содержащий результат преобразования.
     */
    @Override
    public List<SearadarStationMessage> convert(Exchange exchange) {
        return convert(exchange.getIn().getBody(String.class));
    }

    /**
     * Преобразует строковое сообщение в список объектов типа SearadarStationMessage в соответствии с типом сообщения.
     * Входное сообщение анализируется, и в зависимости от его типа создаются соответствующие объекты сообщений,
     * которые добавляются в результирующий список.
     *
     * @param message Строковое сообщение для преобразования.
     * @return Список объектов типа SearadarStationMessage, содержащий результат преобразования.
     */
    public List<SearadarStationMessage> convert(String message) {

        List<SearadarStationMessage> msgList = new ArrayList<>();

        readFields(message);

        switch (msgType) {

            case "TTM" :
                TrackedTargetMessage ttm = getTTM();
                InvalidMessage checkTTM = checkTTM(ttm);

                if (checkTTM != null){
                    msgList.add(checkTTM);
                }
                else{
                    msgList.add(getTTM());
                }
                break;

            case "RSD" : {
                RadarSystemDataMessage rsd = getRSD();
                InvalidMessage invalidMessage = checkRSD(rsd);

                if (invalidMessage != null) {
                    msgList.add(invalidMessage);
                }
                else {
                    msgList.add(rsd);
                }
                break;
            }

        }
        return msgList;
    }

    /**
     * Читает поля из переданного сообщения и извлекает тип сообщения.
     * Поля извлекаются из строки сообщения, начиная с позиции 3 и заканчивая символом "*".
     * Извлеченные поля сохраняются в массиве строк fields, а тип сообщения сохраняется в переменной msgType.
     *
     * @param msg Строковое сообщение, из которого нужно извлечь поля и тип сообщения.
     */
    private void readFields(String msg) {
        String temp = msg.substring( 3, msg.indexOf("*") ).trim();

        fields = temp.split(Pattern.quote(","));
        msgType = fields[0];
    }

    /**
     * Создает и возвращает объект TrackedTargetMessage на основе данных из полей сообщения.
     * Значение времени сообщения устанавливается на текущее время системы.
     * Поля сообщения используются для установки значений свойств объекта TrackedTargetMessage, включая номер цели,
     * расстояние, азимут, курс, скорость, статус цели, код идентификации друг/враг (IFF) и тип цели.
     *
     * @return Объект TrackedTargetMessage, содержащий информацию о трекируемой цели.
     */
    private TrackedTargetMessage getTTM() {
        TrackedTargetMessage ttm = new TrackedTargetMessage();
        Long msgRecTimeMillis = System.currentTimeMillis();

        ttm.setMsgTime(msgRecTimeMillis);
        TargetStatus status = TargetStatus.UNRELIABLE_DATA;
        IFF iff = IFF.UNKNOWN;
        TargetType type = TargetType.UNKNOWN;

        switch (fields[12]) {
            case "L" : status = TargetStatus.LOST;
                break;

            case "Q" : status = TargetStatus.UNRELIABLE_DATA;
                break;

            case "T" : status = TargetStatus.TRACKED;
                break;
        }

        switch (fields[11]) {
            case "b" : iff = IFF.FRIEND;
                break;

            case "p" : iff = IFF.FOE;
                break;

            case "d" : iff = IFF.UNKNOWN;
                break;
        }

        ttm.setMsgRecTime(new Timestamp(System.currentTimeMillis()));
        ttm.setTargetNumber(Integer.parseInt(fields[1]));
        ttm.setDistance(Double.parseDouble(fields[2]));
        ttm.setBearing(Double.parseDouble(fields[3]));
        ttm.setCourse(Double.parseDouble(fields[6]));
        ttm.setSpeed(Double.parseDouble(fields[5]));
        ttm.setStatus(status);
        ttm.setIff(iff);

        ttm.setType(type);

        return ttm;
    }

    /**
     * Создает и возвращает объект RadarSystemDataMessage на основе данных из полей сообщения.
     * Устанавливает время получения сообщения на текущее время системы и устанавливает значения различных свойств
     * объекта RadarSystemDataMessage, таких как начальное расстояние, начальный азимут, движущийся круг расстояния,
     * азимут, расстояние от судна, второй азимут, масштаб расстояния, единицы расстояния, ориентация отображения и
     * рабочий режим.
     *
     * @return Объект RadarSystemDataMessage, содержащий системные данные радара.
     */
    private RadarSystemDataMessage getRSD() {
        RadarSystemDataMessage rsd = new RadarSystemDataMessage();

        rsd.setMsgRecTime(new Timestamp(System.currentTimeMillis()));
        rsd.setInitialDistance(Double.parseDouble(fields[1]));
        rsd.setInitialBearing(Double.parseDouble(fields[2]));
        rsd.setMovingCircleOfDistance(Double.parseDouble(fields[3]));
        rsd.setBearing(Double.parseDouble(fields[4]));
        rsd.setDistanceFromShip(Double.parseDouble(fields[9]));
        rsd.setBearing2(Double.parseDouble(fields[10]));
        rsd.setDistanceScale(Double.parseDouble(fields[11]));
        rsd.setDistanceUnit(fields[12]);
        rsd.setDisplayOrientation(fields[13]);
        rsd.setWorkingMode(fields[14]);

        return rsd;
    }

    /**
     * Проверяет данные сообщения TrackedTargetMessage на корректность.
     * Если данные не соответствуют допустимым значениям, создает объект InvalidMessage с информацией об ошибке.
     *
     * @param ttm Объект TrackedTargetMessage для проверки.
     * @return Объект InvalidMessage с информацией об ошибке, если данные сообщения некорректны; в противном случае null.
     */
    private InvalidMessage checkTTM(TrackedTargetMessage ttm){
        InvalidMessage invalidMessage = new InvalidMessage();

        if (ttm.getTargetNumber() < 1 || ttm.getTargetNumber() > 50){
            invalidMessage.setInfoMsg("TTM message. Wrong distance (01-50): " + ttm.getTargetNumber());
            return invalidMessage;
        }

        if (ttm.getDistance() < 0.0 || ttm.getDistance() > 32.0){
            invalidMessage.setInfoMsg("TTM message. Wrong distance (0.0-32.0): " + ttm.getDistance());
            return invalidMessage;
        }

        if (ttm.getBearing() < 0.0 || ttm.getBearing() > 359.9){
            invalidMessage.setInfoMsg("TTM message. Wrong bearing (0.0-359.9): " + ttm.getBearing());
            return invalidMessage;
        }

        if (ttm.getSpeed() < 0.0 || ttm.getSpeed() > 90.0){
            invalidMessage.setInfoMsg("TTM message. Wrong speed (0.0-90.0): " + ttm.getSpeed());
            return invalidMessage;
        }

        if (ttm.getCourse() < 0.0 || ttm.getCourse() > 359.9){
            invalidMessage.setInfoMsg("TTM message. Wrong course (0.0-359.9): " + ttm.getCourse());
            return invalidMessage;
        }
        return null;
    }

    /**
     * Проверяет данные сообщения RadarSystemDataMessage на корректность.
     * Если значение масштаба расстояния не находится в списке допустимых значений, создает объект InvalidMessage
     * с информацией об ошибке.
     *
     * @param rsd Объект RadarSystemDataMessage для проверки.
     * @return Объект InvalidMessage с информацией об ошибке, если значение масштаба расстояния некорректно;
     * в противном случае null.
     */
    private InvalidMessage checkRSD(RadarSystemDataMessage rsd) {
        InvalidMessage invalidMessage = new InvalidMessage();
        String infoMsg = "";

        if (!Arrays.asList(DISTANCE_SCALE).contains(rsd.getDistanceScale())) {
            infoMsg = "RSD message. Wrong distance scale value: " + rsd.getDistanceScale();
            invalidMessage.setInfoMsg(infoMsg);
            return invalidMessage;
        }
        return null;
    }
}
