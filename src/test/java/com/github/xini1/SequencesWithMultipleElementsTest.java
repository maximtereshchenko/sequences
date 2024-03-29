package com.github.xini1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.github.xini1.accumulator.base.ListAccumulator;
import com.github.xini1.sequence.base.ArraySequence;
import com.github.xini1.sequence.base.GeneratedSequence;
import com.github.xini1.sequence.base.Sequence;
import org.assertj.core.api.Assertions;
import com.github.xini1.sequence.base.IterableSequence;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Maxim Tereshchenko
 */
final class SequencesWithMultipleElementsTest {

    private static List<Arguments> sequences() {
        var numbers = List.of(1, 1, 3, 2, 3, 4);
        var counter = new AtomicInteger(0);

        return List.of(
                arguments(new IterableSequence<>(numbers)),
                Arguments.arguments(new ArraySequence<>(numbers.toArray(Integer[]::new))),
                Arguments.arguments(
                        new GeneratedSequence<>(0, index -> index < numbers.size(), index -> index + 1)
                                .map(numbers::get)
                ),
                arguments(
                        new GeneratedSequence<>(0, index -> index + 1)
                                .limit(6)
                                .map(numbers::get)
                ),
                arguments(
                        new GeneratedSequence<>(counter::getAndIncrement)
                                .limit(6)
                                .map(numbers::get)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenNumbers_whenFold_thenSameNumbersInList(Sequence<Integer> sequence) {
        Assertions.assertThat(sequence.fold(new ListAccumulator<>())).containsExactly(1, 1, 3, 2, 3, 4);
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenEvenAndOddNumbers_whenFilter_thenOnlyEvens(Sequence<Integer> sequence) {
        Assertions.assertThat(
                sequence.filter(num -> num % 2 == 0)
                        .fold(new ListAccumulator<>())
        )
                .containsExactly(2, 4);
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenNumbers_whenMap_thenNumbersIncremented(Sequence<Integer> sequence) {
        Assertions.assertThat(
                sequence.map(num -> num + 1)
                        .fold(new ListAccumulator<>())
        )
                .containsExactly(2, 2, 4, 3, 4, 5);
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenNumbers_whenFlatMap_thenTwoTimesMoreNumbers(Sequence<Integer> sequence) {
        Assertions.assertThat(
                sequence.flatMap(num -> new ArraySequence<>(num, num + 1))
                        .fold(new ListAccumulator<>())
        )
                .containsExactly(1, 2, 1, 2, 3, 4, 2, 3, 3, 4, 4, 5);
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenNumbersWithDuplicates_whenDistinct_thenUniqueNumbers(Sequence<Integer> sequence) {
        Assertions.assertThat(
                sequence.distinct()
                        .fold(new ListAccumulator<>())
        )
                .containsExactly(1, 3, 2, 4);
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenUnsortedNumbers_whenSorted_thenNumbersSorted(Sequence<Integer> sequence) {
        Assertions.assertThat(
                sequence.sorted(Comparator.naturalOrder())
                        .fold(new ListAccumulator<>())
        )
                .containsExactly(1, 1, 2, 3, 3, 4);
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenNumbers_whenOnEach_thenCounterIncremented(Sequence<Integer> sequence) {
        var counter = new AtomicInteger();

        sequence.onEach(unused -> counter.incrementAndGet())
                .fold(new ListAccumulator<>());

        assertThat(counter.get()).isEqualTo(6);
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenNumbers_whenForEach_thenNumbersAddedToList(Sequence<Integer> sequence) {
        var list = new ArrayList<Integer>();

        sequence.forEach(list::add);

        assertThat(list).containsExactly(1, 1, 3, 2, 3, 4);
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenManyNumbers_whenLimit_thenThereAreLessThenLimitSizeElements(Sequence<Integer> sequence) {
        Assertions.assertThat(
                sequence.limit(2)
                        .fold(new ListAccumulator<>())
        )
                .containsExactly(1, 1);
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenManyNumbers_whenSkip_thenFirstElementsSkipped(Sequence<Integer> sequence) {
        Assertions.assertThat(
                sequence.skip(2)
                        .fold(new ListAccumulator<>())
        )
                .containsExactly(3, 2, 3, 4);
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenManyNumbers_whenTakeWhileLessThan3_thenNumbersBefore3Remained(Sequence<Integer> sequence) {
        Assertions.assertThat(
                sequence.takeWhile(num -> num < 3)
                        .fold(new ListAccumulator<>())
        )
                .containsExactly(1, 1);
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenManyNumbers_whenDropWhileLessThan3_thenNumbersBefore3Skipped(Sequence<Integer> sequence) {
        Assertions.assertThat(
                sequence.dropWhile(num -> num < 3)
                        .fold(new ListAccumulator<>())
        )
                .containsExactly(3, 2, 3, 4);
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenFlatMappedSequence_whenSkip_thenSkipOnlyFirstElementsOfFlatMappedSequence(Sequence<Integer> sequence) {
        Assertions.assertThat(
                sequence.flatMap(num -> new ArraySequence<>(num, num))
                        .skip(5)
                        .fold(new ListAccumulator<>())
        )
                .containsExactly(3, 2, 2, 3, 3, 4, 4);
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenFlatMappedSequence_whenLimit_thenThereAreLessThenLimitSizeElements(Sequence<Integer> sequence) {
        Assertions.assertThat(
                sequence.flatMap(num -> new ArraySequence<>(num, num))
                        .limit(5)
                        .fold(new ListAccumulator<>())
        )
                .containsExactly(1, 1, 1, 1, 3);
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenFlatMappedSequenceWithDuplicates_whenDistinct_thenUniqueNumbers(Sequence<Integer> sequence) {
        Assertions.assertThat(
                sequence.flatMap(num -> new ArraySequence<>(num, num + 1))
                        .distinct()
                        .fold(new ListAccumulator<>())
        )
                .containsExactly(1, 2, 3, 4, 5);
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenUnsortedFlatMappedSequence_whenSorted_thenNumbersSorted(Sequence<Integer> sequence) {
        Assertions.assertThat(
                sequence.flatMap(num -> new ArraySequence<>(num, num + 1))
                        .sorted(Comparator.naturalOrder())
                        .fold(new ListAccumulator<>())
        )
                .containsExactly(1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5);
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenNumbers_whenMinimum_thenMinimumNumberIsFound(Sequence<Integer> sequence) {
        assertThat(sequence.minimum(Comparator.naturalOrder())).contains(1);
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenFlatMappedNumbers_whenMinimum_thenMinimumNumberIsFound(Sequence<Integer> sequence) {
        assertThat(
                sequence.flatMap(num -> new ArraySequence<>(-num, num))
                        .minimum(Comparator.naturalOrder())
        )
                .contains(-4);
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenEmptySequence_whenMinimum_thenEmptyOptionalReturned(Sequence<Integer> sequence) {
        assertThat(
                sequence.skip(6)
                        .minimum(Comparator.naturalOrder())
        )
                .isEmpty();
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenNumbers_whenCount_thenNumbersCounted(Sequence<Integer> sequence) {
        assertThat(sequence.count()).isEqualTo(6);
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenFlatMappedNumbers_whenCount_thenNumbersCounted(Sequence<Integer> sequence) {
        assertThat(
                sequence.flatMap(num -> new ArraySequence<>(num, num))
                        .count()
        )
                .isEqualTo(12);
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenNumbers_whenFindFirst_thenFirstNumberIsFound(Sequence<Integer> sequence) {
        assertThat(sequence.findFirst()).contains(1);
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenNumbersLessThan5_whenAnyMatchGreaterThan5_thenFalseReturned(Sequence<Integer> sequence) {
        assertThat(sequence.anyMatch(num -> num > 5)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenNumbersLessThan5_whenAnyMatchLessThan5_thenTrueReturned(Sequence<Integer> sequence) {
        assertThat(sequence.anyMatch(num -> num < 5)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenEmptySequence_whenAnyMatch_thenFalseReturned(Sequence<Integer> sequence) {
        assertThat(
                sequence.skip(6)
                        .anyMatch(num -> num < 5)
        )
                .isFalse();
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenNumbersLessThan5_whenAllMatchGreaterThan5_thenFalseReturned(Sequence<Integer> sequence) {
        assertThat(sequence.allMatch(num -> num > 5)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenNumbersLessThan5_whenAllMatchLessThan5_thenTrueReturned(Sequence<Integer> sequence) {
        assertThat(sequence.allMatch(num -> num < 5)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenEmptySequence_whenAllMatch_thenTrueReturned(Sequence<Integer> sequence) {
        assertThat(
                sequence.skip(6)
                        .allMatch(num -> num > 5)
        )
                .isTrue();
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenNumbersLessThan5_whenNoneMatchLessThan5_thenFalseReturned(Sequence<Integer> sequence) {
        assertThat(sequence.noneMatch(num -> num < 5)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenNumbersLessThan5_whenNoneMatchGreaterThan5_thenTrueReturned(Sequence<Integer> sequence) {
        assertThat(sequence.noneMatch(num -> num > 5)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("sequences")
    void givenEmptySequence_whenNoneMatchEven_thenTrueReturned(Sequence<Integer> sequence) {
        assertThat(
                sequence.skip(6)
                        .noneMatch(num -> num < 5)
        )
                .isTrue();
    }
}