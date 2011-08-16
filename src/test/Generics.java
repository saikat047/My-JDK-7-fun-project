package test;

import sun.net.www.content.text.Generic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:saikat@viatravel.no">Kazi Abdullah Saikat</a>
 */
public class Generics {
    public static void main(String[] pArgv) throws Exception {
        new Generics().testIt();
    }

    public static <E extends Animal> void printAnimals(List<E> animals) {
        for (Animal animal : animals) {
            System.out.println("animal = " + animal);
        }
    }

    public void testIt() {
        List<Animal> animals = new ArrayList<>();
        animals.add(new Human());
        printAnimals(animals);

        List<? super Animal> wildAnimals = new ArrayList<>();
        wildAnimals.add(new Woman());
        printAnimals(animals);

        wildAnimals.addAll(new ArrayList<Human>());

        wildAnimals.addAll(new ArrayList<Man>());

        List<? extends Man> stupidMen = new LinkedList<>();
        wildAnimals.addAll(stupidMen);
        printAnimals(stupidMen);

        List<? extends Human> manAndWoman = new LinkedList<>();

        List<? extends Human> socialAnimals = new LinkedList<Man>();

        // Generics.printAnimals(new ArrayList<>()); won't compile,
        // automatic type inference doesn't work for parameter passing.
        Generics.<Human>printAnimals(new ArrayList<Human>());
        Generics.printAnimals(new ArrayList<Human>());
    }

    abstract class Animal {
    }

    class Human extends Animal {
    }

    class Man extends Human {
    }

    class Woman extends Human implements InfiniteComplexity {
        @Override
        public void doVoidAndComplexThings() throws Exception {

        }
    }

    interface InfiniteComplexity {
        public void doVoidAndComplexThings() throws Exception;
    }
}
