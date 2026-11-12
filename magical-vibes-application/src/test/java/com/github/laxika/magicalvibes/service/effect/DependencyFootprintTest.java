package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.service.effect.DependencyFootprint.BoardTable;
import com.github.laxika.magicalvibes.service.effect.DependencyFootprint.Characteristic;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DependencyFootprintTest {

    private final UUID creature = UUID.randomUUID();
    private final UUID equipment = UUID.randomUUID();

    @Test
    @DisplayName("A write to a characteristic another trial read can change it")
    void writeToReadCharacteristicConflicts() {
        DependencyFootprint writer = new DependencyFootprint();
        writer.write(Characteristic.ABILITIES, creature);
        DependencyFootprint reader = new DependencyFootprint();
        reader.read(Characteristic.ABILITIES, creature);

        assertThat(writer.mayChangeWhatIsReadBy(reader)).isTrue();
        assertThat(reader.mayChangeWhatIsReadBy(writer)).isFalse();
    }

    @Test
    @DisplayName("Writes to another permanent or another characteristic cannot change what was read")
    void disjointAccessesDoNotConflict() {
        DependencyFootprint writer = new DependencyFootprint();
        writer.write(Characteristic.ABILITIES, creature);
        DependencyFootprint reader = new DependencyFootprint();
        reader.read(Characteristic.ABILITIES, equipment);
        reader.read(Characteristic.TYPES, creature);

        assertThat(writer.mayChangeWhatIsReadBy(reader)).isFalse();
    }

    @Test
    @DisplayName("Two Equipment granting abilities to the creature they share do not read each other's writes")
    void equipmentGrantsToOneCreatureDoNotConflict() {
        DependencyFootprint first = new DependencyFootprint();
        first.read(Characteristic.ABILITIES, equipment);
        first.write(Characteristic.ABILITIES, creature);
        first.write(BoardTable.L56_TOUCHED, creature);
        DependencyFootprint second = new DependencyFootprint();
        second.read(Characteristic.ABILITIES, UUID.randomUUID());
        second.write(Characteristic.ABILITIES, creature);
        second.write(BoardTable.L56_TOUCHED, creature);

        assertThat(first.mayChangeWhatIsReadBy(second)).isFalse();
        assertThat(second.mayChangeWhatIsReadBy(first)).isFalse();
    }

    @Test
    @DisplayName("A side table read as a whole conflicts with any write to it")
    void wholeTableReadConflictsWithAnyWrite() {
        DependencyFootprint writer = new DependencyFootprint();
        writer.write(BoardTable.MARCH_ANIMATED_IDS, creature);
        DependencyFootprint reader = new DependencyFootprint();
        reader.readWholeTable(BoardTable.MARCH_ANIMATED_IDS);

        assertThat(writer.mayChangeWhatIsReadBy(reader)).isTrue();
    }

    @Test
    @DisplayName("Read-tracking side tables record keys read and the whole table when iterated, not mutations")
    void readTrackingTablesRecordReads() {
        DependencyFootprint reads = new DependencyFootprint();
        Set<UUID> touched = new ReadTrackingSet<>(new HashSet<>(), BoardTable.L56_TOUCHED, reads);
        Map<UUID, Integer> devotion = new ReadTrackingMap<>(new HashMap<>(), BoardTable.DEVOTION_MODIFIERS, reads);

        touched.add(creature);
        devotion.put(creature, 1);

        assertThat(writing(BoardTable.L56_TOUCHED, creature).mayChangeWhatIsReadBy(reads)).isFalse();
        assertThat(writing(BoardTable.DEVOTION_MODIFIERS, creature).mayChangeWhatIsReadBy(reads)).isFalse();

        touched.contains(creature);
        devotion.getOrDefault(equipment, 0);

        assertThat(writing(BoardTable.L56_TOUCHED, creature).mayChangeWhatIsReadBy(reads)).isTrue();
        assertThat(writing(BoardTable.DEVOTION_MODIFIERS, equipment).mayChangeWhatIsReadBy(reads)).isTrue();
        assertThat(writing(BoardTable.DEVOTION_MODIFIERS, creature).mayChangeWhatIsReadBy(reads)).isFalse();

        devotion.forEach((id, amount) -> { });

        assertThat(writing(BoardTable.DEVOTION_MODIFIERS, creature).mayChangeWhatIsReadBy(reads)).isTrue();
    }

    private static DependencyFootprint writing(BoardTable table, UUID key) {
        DependencyFootprint footprint = new DependencyFootprint();
        footprint.write(table, key);
        return footprint;
    }
}
