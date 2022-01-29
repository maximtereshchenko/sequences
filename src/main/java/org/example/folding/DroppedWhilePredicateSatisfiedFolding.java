package org.example.folding;

import org.example.accumulator.DroppingWhilePredicateSatisfiedAccumulator;
import org.example.accumulator.base.Accumulator;

import java.util.function.Predicate;

/**
 * @author Maxim Tereshchenko
 */
public final class DroppedWhilePredicateSatisfiedFolding<T> implements Folding<T> {

    private final Folding<T> original;
    private final Predicate<T> predicate;

    public DroppedWhilePredicateSatisfiedFolding(Folding<T> original, Predicate<T> predicate) {
        this.original = original;
        this.predicate = predicate;
    }

    @Override
    public <R> R fold(Accumulator<T, R> accumulator) {
        return original.fold(new DroppingWhilePredicateSatisfiedAccumulator<>(accumulator, predicate));
    }
}
