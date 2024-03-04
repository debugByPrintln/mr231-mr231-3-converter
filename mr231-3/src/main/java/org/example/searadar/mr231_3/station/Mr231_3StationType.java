package org.example.searadar.mr231_3.station;

import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;

import org.example.searadar.mr231_3.convert.Mr231_3Converter;

import java.nio.charset.Charset;

/**
 * Класс, представляющий тип станции МР-231-3.
 * Этот класс содержит константы, определяющие тип и название кодека для станции МР-231-3.
 * Также класс предоставляет методы для инициализации и создания парсера для станции МР-231-3.
 */
public class Mr231_3StationType {
    /**
     * Константа, определяющая тип станции МР-231-3
     */
    private static final String STATION_TYPE = "МР-231-3";

    /**
     * Константа, определяющая название кодека для станции МР-231-3
     */
    private static final String CODEC_NAME = "mr231-3";

    /**
     * Инициализирует класс для работы со станцией МР-231-3.
     * Внутренний метод, используемый для установки параметров кодека.
     */
    protected void doInitialize() {
        TextLineCodecFactory textLineCodecFactory = new TextLineCodecFactory(
                Charset.defaultCharset(),
                LineDelimiter.UNIX,
                LineDelimiter.CRLF
        );
    }

    /**
     * Создает конвертер для станции МР-231-3.
     *
     * @return Объект Mr231_3Converter, предназначенный для преобразования данных станции МР-231-3.
     */
    public Mr231_3Converter createConverter() {
        return new Mr231_3Converter();
    }
}
