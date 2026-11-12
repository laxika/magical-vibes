package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.service.effect.DependencyFootprint.BoardTable;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

/**
 * One of a dependency trial's layered-board side tables, recording into the trial's
 * {@link DependencyFootprint} each element it is asked about, and the whole table when it is
 * iterated or sized. Mutations go straight to the table unrecorded: the trial finds what it wrote
 * by comparing the table with its starting content afterwards, which also catches writes made
 * through an iterator. A mutator's return value is not recorded as a read, so code that decides
 * something by it ({@code if (set.add(x))}) asks {@link #contains} first.
 */
final class ReadTrackingSet<E> extends AbstractSet<E> {

    private final Set<E> table;
    private final BoardTable tag;
    private final DependencyFootprint footprint;

    ReadTrackingSet(Set<E> table, BoardTable tag, DependencyFootprint footprint) {
        this.table = table;
        this.tag = tag;
        this.footprint = footprint;
    }

    @Override
    public boolean contains(Object element) {
        footprint.read(tag, element);
        return table.contains(element);
    }

    @Override
    public boolean containsAll(Collection<?> elements) {
        elements.forEach(element -> footprint.read(tag, element));
        return table.containsAll(elements);
    }

    @Override
    public Iterator<E> iterator() {
        footprint.readWholeTable(tag);
        return table.iterator();
    }

    @Override
    public int size() {
        footprint.readWholeTable(tag);
        return table.size();
    }

    @Override
    public boolean isEmpty() {
        footprint.readWholeTable(tag);
        return table.isEmpty();
    }

    @Override
    public boolean add(E element) {
        return table.add(element);
    }

    @Override
    public boolean remove(Object element) {
        return table.remove(element);
    }

    @Override
    public boolean addAll(Collection<? extends E> elements) {
        return table.addAll(elements);
    }

    @Override
    public boolean removeAll(Collection<?> elements) {
        return table.removeAll(elements);
    }

    @Override
    public boolean retainAll(Collection<?> elements) {
        return table.retainAll(elements);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        footprint.readWholeTable(tag);
        return table.removeIf(filter);
    }

    @Override
    public void clear() {
        table.clear();
    }
}
