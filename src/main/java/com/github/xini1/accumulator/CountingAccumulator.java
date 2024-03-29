package com.github.xini1.accumulator;

import com.github.xini1.accumulator.base.Accumulator;

/**
 * @author Maxim Tereshchenko
 */
public final class CountingAccumulator<T> implements Accumulator<T, Long> {

    private final long runningTotal;

    private CountingAccumulator(long runningTotal) {
        this.runningTotal = runningTotal;
    }

    public CountingAccumulator() {
        this(0);
    }

    @Override
    public boolean canAccept() {
        return true;
    }

    @Override
    public Accumulator<T, Long> onElement(T element) {
        return new CountingAccumulator<>(runningTotal + 1);
    }

    @Override
    public Long onFinish() {
        return runningTotal;
    }
}
