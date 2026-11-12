package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.service.effect.DependencyFootprint.BoardTable;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * The map counterpart of {@link ReadTrackingSet}: records into the trial's
 * {@link DependencyFootprint} each key it is asked about, including through
 * {@code computeIfAbsent} and the other read-modify-write operations, and the whole table when it is
 * iterated, sized or searched by value. Plain {@code put}, {@code remove} and {@code clear} go
 * straight to the table unrecorded, as the trial finds its writes by comparing the table with its
 * starting content. A nested map read through {@link #get} is recorded under its outer key.
 */
final class ReadTrackingMap<K, V> extends AbstractMap<K, V> {

    private final Map<K, V> table;
    private final BoardTable tag;
    private final DependencyFootprint footprint;

    ReadTrackingMap(Map<K, V> table, BoardTable tag, DependencyFootprint footprint) {
        this.table = table;
        this.tag = tag;
        this.footprint = footprint;
    }

    @Override
    public V get(Object key) {
        footprint.read(tag, key);
        return table.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        footprint.read(tag, key);
        return table.containsKey(key);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        footprint.read(tag, key);
        return table.getOrDefault(key, defaultValue);
    }

    @Override
    public boolean containsValue(Object value) {
        footprint.readWholeTable(tag);
        return table.containsValue(value);
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
    public Set<Entry<K, V>> entrySet() {
        footprint.readWholeTable(tag);
        return table.entrySet();
    }

    @Override
    public Set<K> keySet() {
        footprint.readWholeTable(tag);
        return table.keySet();
    }

    @Override
    public Collection<V> values() {
        footprint.readWholeTable(tag);
        return table.values();
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        footprint.readWholeTable(tag);
        table.forEach(action);
    }

    @Override
    public V put(K key, V value) {
        return table.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return table.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> entries) {
        table.putAll(entries);
    }

    @Override
    public void clear() {
        table.clear();
    }

    @Override
    public V putIfAbsent(K key, V value) {
        footprint.read(tag, key);
        return table.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        footprint.read(tag, key);
        return table.remove(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        footprint.read(tag, key);
        return table.replace(key, oldValue, newValue);
    }

    @Override
    public V replace(K key, V value) {
        footprint.read(tag, key);
        return table.replace(key, value);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        footprint.readWholeTable(tag);
        table.replaceAll(function);
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        footprint.read(tag, key);
        return table.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        footprint.read(tag, key);
        return table.computeIfPresent(key, remappingFunction);
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        footprint.read(tag, key);
        return table.compute(key, remappingFunction);
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        footprint.read(tag, key);
        return table.merge(key, value, remappingFunction);
    }
}
