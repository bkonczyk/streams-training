package pl.sda.streams;

import pl.sda.streams.model.CsvFile;
import pl.sda.streams.model.CsvLine;
import pl.sda.streams.model.Person;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class PersonReader {
    public List<Person> readFromFile(File fileName) throws ParseException {
        CsvFile csvLines = CsvFile.fromFile(fileName);
        List<Person> persons = new ArrayList<>();
        long start = System.currentTimeMillis();
        for (CsvLine csvLine : csvLines) {
            persons.add(new Person(csvLine));
        }
        long stop = System.currentTimeMillis();
        System.out.println("Converted " + persons.size() + " in " + (stop - start) + " ms");
        return persons;
    }
}
