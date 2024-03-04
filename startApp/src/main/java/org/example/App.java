package org.example;

import org.example.searadar.mr231.convert.Mr231Converter;
import org.example.searadar.mr231.station.Mr231StationType;
import org.example.searadar.mr231_3.convert.Mr231_3Converter;
import org.example.searadar.mr231_3.station.Mr231_3StationType;
import ru.oogis.searadar.api.message.SearadarStationMessage;

import java.util.List;

/**
 * Практическое задание направлено на проверку умения создавать функциональные модули и работать с технической
 * документацией.
 * Задача написать парсер сообщений для протокола МР-231-3 на основе парсера МР-231.
 * Приложение к заданию файлы:
 * - Протокол МР-231.docx
 * - Протокол МР-231-3.docx
 * <p>
 * Требования:
 * 1. Перенести контрольный пример из "Протокола МР-231.docx" в метод main, по образцу в методе main;
 * 2. Проверить правильность работы конвертера путём вывода контрольного примера в консоль, по образцу в методе main;
 * 3. Создать модуль с именем mr-231-3 и добавить его в данный проект <module>../mr-231-3</module> и реализовать его
 * функционал в соответствии с "Протоколом МР-231-3.docx" на подобии модуля mr-231;
 * 4. Создать для модуля контрольный пример и проверить правильность работы конвертера путём вывода контрольного
 * примера в консоль
 *
 * <p>
 *     Примечание:
 *     1. Данные в пакете ru.oogis не изменять!!!
 *     2. Весь код должен быть покрыт JavaDoc
 */

public class App {
    public static void main(String[] args) {
        System.out.println("Контрольный пример для МР-231-3:");
        // Контрольный пример для МР-231
        String mr231_TTM = "$RATTM,23,13.88,137.2,T,63.8,094.3,T,9.2,79.4,N,b,T,,783344,А*42";
        String mr231_VHW = "$RAVHW,115.6,T,,,46.0,N,,*71";
        String mr231_RSD = "$RARSD,36.5,331.4,8.4,320.6,,,,,11.6,185.3,96.0,N,N,S*33";

        // Проверка работы конвертера
        Mr231StationType mr231 = new Mr231StationType();
        Mr231Converter mr231Converter = mr231.createConverter();

        List<SearadarStationMessage> searadarMessages = mr231Converter.convert(mr231_TTM);
        searadarMessages.forEach(System.out::println);

        searadarMessages = mr231Converter.convert(mr231_VHW);
        searadarMessages.forEach(System.out::println);

        searadarMessages = mr231Converter.convert(mr231_RSD);
        searadarMessages.forEach(System.out::println);

        System.out.println("Контрольный пример для МР-231-3:");

        // Контрольный пример для МР-231-3
        String mr231_3_TTM = "$RATTM,23,13.88,137.2,T,63.8,094.3,T,9.2,79.4,N,b,T,,783344,А*42";
        String mr231_3_RSD = "$RARSD,36.5,331.4,8.4,320.6,,,,,11.6,185.3,96.0,N,N,S*33";

        // Проверка работы конвертера МР-231-3
        Mr231_3StationType mr231_3 = new Mr231_3StationType();
        Mr231_3Converter mr231_3Converter = mr231_3.createConverter();

        searadarMessages = mr231_3Converter.convert(mr231_3_TTM);
        searadarMessages.forEach(System.out::println);

        searadarMessages = mr231_3Converter.convert(mr231_3_RSD);
        searadarMessages.forEach(System.out::println);
    }
}
