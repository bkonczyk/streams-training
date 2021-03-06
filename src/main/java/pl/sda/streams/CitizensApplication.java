package pl.sda.streams;

import pl.sda.streams.model.Person;

import java.io.File;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class CitizensApplication {

    private PersonReader personReader;

    public CitizensApplication() {
        this.personReader = new PersonReader();
    }

    public void process() throws ParseException {
        File personFile = new File(getClass().getClassLoader().getResource("person.csv").getFile());
        List<Person> people = personReader.readFromFile(personFile);

        Map<String, Long> nameSummary = createNameSummary(people);
        System.out.println(getPeopleWithGivenLastName(people, "czarnecki"));
        System.out.println(getPeopleWithGivenLastNameWithStream(people, "czarnecki"));
        Map<String, List<Person>> stringListMap = groupPeopleByNames(people);
        System.out.println(countPeopleWhoCanRetire(people));
        System.out.println("EXIT");

//        System.out.println(AnotherClass.countDistinctLastNames(people));
//        System.out.println(AnotherClass.countDistinctLastNames2(people));
        System.out.println(countDistinctLastNames2(people));
        System.out.println(countDistinctLastNames(people));
        Map<String, List<Person>> pplBySex = groupPeopleBySex(people);
        System.out.println("");
        // sample: how to print results to the console
//        for (Map.Entry<String, Long> nameCount : nameSummary.entrySet()) {
//            System.out.println("There are " + nameCount.getValue() + " persons with name " + nameCount.getKey());
//        }

        Map<String, Double> averageAgeBySex = averageAgeBySex(people);
        System.out.println("#############");
        System.out.println("Length of longest name: " + getTheLongestLastName(people).length());
        System.out.println("Longest last name: " + getTheLongestLastName(people));
        System.out.println("List of longest last names: " + collectAllLastNamesWithLength13(people));
        System.out.println("Total age of all males: " + collectTotalAgeOfAllMales(people));
    }

    private Map<String, Long> createNameSummary(List<Person> people) {
        Map<String, Long> nameCount = new HashMap<>();
        for (Person person : people) {
            String name = person.getName();
            Long peopleWithTheSameNameCount = nameCount.getOrDefault(name, 0L);
            peopleWithTheSameNameCount++;
            nameCount.put(name, peopleWithTheSameNameCount);
        }
        return nameCount;
    }
// W klasie CitizensApplication napisz metod??, kt??ra b??dzie zlicza??, ile jest os??b z danym nazwiskiem.

    private Long getPeopleWithGivenLastName(List<Person> people, String lastName) {
        Long count = 0L;
        for (Person person : people) {
            if (person.getLastName().equalsIgnoreCase(lastName)) {
                count++;
            }
        }
        return count;
    }

    private Long getPeopleWithGivenLastNameWithStream(List<Person> people, String lastName) {
        return people.stream()
                .filter(person -> person.getLastName().equalsIgnoreCase(lastName))
                .count();
    }

// kt??ra b??dzie grupowa?? obiekty klasy Person po imieniu.

    private Map<String, List<Person>> groupPeopleByNames(List<Person> people) {
        return people.stream()
                .collect(Collectors.groupingBy(Person::getName));
    }
//metod??, kt??ra policzy osoby mog??ce ubiega?? si?? o emerytur?? (60 lat dla kobiet oraz 65 dla m????czyzn)

    private Long countPeopleWhoCanRetire(List<Person> people) {
        LocalDateTime now = LocalDate.now().atStartOfDay();
        return people.stream()
                .filter(person -> isRetirableFemale(now, person) || isRetirableMale(now, person))
                .count();
    }

    private boolean isRetirableFemale(LocalDateTime now, Person p) {
        LocalDateTime birthDate = convertToLocalDateViaInstant(p.getBirthDate()).atStartOfDay();
        return p.getSex().equals("F") && Duration.between(birthDate, now).toDays() > 21914;
    }

    private boolean isRetirableMale(LocalDateTime now, Person p) {
        LocalDateTime birthDate = convertToLocalDateViaInstant(p.getBirthDate()).atStartOfDay();
        return p.getSex().equals("M") && Duration.between(birthDate, now).toDays() > 23740;
    }

    public LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    //napisz metod??, kt??ra b??dzie filtrowa?? osoby tak, aby zosta??y tylko te maj??ce wi??cej ni?? 35 oraz mniej ni?? 55 lat.
    private List<Person> peopleOfCertainAge(List<Person> people) {
        LocalDateTime now = LocalDate.now().atStartOfDay();
        return people.stream()
                .filter(person -> is35To55(person, now))
                .collect(Collectors.toList());
    }

    private boolean is35To55(Person person, LocalDateTime now) {
        LocalDateTime birthDate = convertToLocalDateViaInstant(person.getBirthDate()).atStartOfDay();
        return Duration.between(birthDate, now).toDays() > 12783 && Duration.between(birthDate, now).toDays() < 20088;
    }

    //policz ile jest r????nych nazwisk

    private Long countDistinctLastNames(List<Person> people) {
        long start = System.currentTimeMillis();
        long count = people.stream()
                .map(Person::getLastName)
                .distinct()
                .count();
        long stop = System.currentTimeMillis();
        System.out.println("Count calculated in " + (stop - start) + " ms");
        return count;
    }

    private int countDistinctLastNames2(List<Person> people) {
        long start = System.currentTimeMillis();
        int count = people.stream()
                .map(Person::getLastName)
                .collect(Collectors.toSet())
                .size();
        long stop = System.currentTimeMillis();
        System.out.println("Count with set calculated in " + (stop - start) + " ms");
        return count;
    }

    private Map<String, List<String>> groupLastNamesByFirstNames(List<Person> people) {
        return people.stream()
                .collect(Collectors.groupingBy(
                        Person::getName,
                        Collectors.mapping(Person::getLastName, Collectors.toList())));
    }

    private Map<String, List<Person>> groupPeopleBySex(List<Person> people) {
        return people.stream()
                .collect(Collectors.groupingBy(Person::getSex));
    }

    public static Map<String, Double> averageAgeBySex(List<Person> people) {
        return people.stream()
                .filter(person -> Set.of("M", "F").contains(person.getSex()))
                .collect(Collectors.groupingBy(
                        Person::getSex,
                        Collectors.averagingDouble(Person::getAge)
                ));
    }

    private List<Person> collectWomenOlderThan40(List<Person> people) {
        return people.stream()
                .filter(person -> "F".equals(person.getSex()) && person.getAge() > 40)
                .collect(Collectors.toList());
    }

    private String getTheLongestLastName(List<Person> people) {
        return people.stream()
                .map(Person::getLastName)
                .max(Comparator.comparing(String::length))
                .orElse("");
    }

    private Set<String> collectAllLastNamesWithLength13(List<Person> people) {
        return people.stream()
                .map(Person::getLastName)
                .filter(whatever -> whatever.length() == 13)
                .collect(Collectors.toSet());
    }

    private Long countAllNonMaleNonFemaleObjects(List<Person> people) {
        return people.stream()
                .filter(p -> !Set.of("M", "F").contains(p.getSex()))
                .count();
    }

    private Long collectTotalAgeOfAllMales(List<Person> people) {
        return people.stream()
                .filter(p -> "M".equals(p.getSex()))
                .map(Person::getAge)
                .mapToLong(Long::longValue)
                .sum();
    }
}
