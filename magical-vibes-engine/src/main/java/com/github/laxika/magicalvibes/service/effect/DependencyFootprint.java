package com.github.laxika.magicalvibes.service.effect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * What one effect instance read and wrote in its base CR 613.8 dependency trial in
 * {@link LayerSystemService}: per permanent and {@link Characteristic} for the trial states, and per
 * key for the layered board's {@link BoardTable}s.
 *
 * <p>Applying B first can only change A's fingerprint if B writes something A reads: until A's
 * trial reads a value B changed, it runs exactly as in its own base trial, reading the same values
 * and recording the same operations. {@link #mayChangeWhatIsReadBy} is that test, and pairs that
 * fail it skip their trial. A table read as a whole (iteration, size) conflicts with any write to
 * the table.</p>
 */
final class DependencyFootprint {

    /** The characteristics a trial state's reads and writes are recorded under. */
    enum Characteristic {
        NAME, TYPES, COLORS, ABILITIES, POWER_TOUGHNESS, TOKEN
    }

    /**
     * The layered board's side tables whose keys a trial reads. The provenance tables are not
     * among them: they are display output, read only after the pass.
     */
    enum BoardTable {
        LAND_TYPE_OVERRIDES, MARCH_ANIMATED_IDS, STARFIELD_ANIMATED_IDS, OPALESCENCE_ANIMATED_IDS,
        MANAGED_L4_EFFECTS, L4_CONTRIBUTIONS, L4_FILTER_VERDICTS, MANAGED_L56_EFFECTS, L56_TOUCHED,
        BASE_PT_7B, SWITCHED_PT_7D, DEVOTION_MODIFIERS, SELF_INFLICTED_LOSE_ALL
    }

    private static final Object WHOLE_TABLE = new Object() {
        @Override
        public String toString() {
            return "WHOLE_TABLE";
        }
    };

    private final Map<Object, Set<Object>> reads = new HashMap<>();
    private final Map<Object, Set<Object>> writes = new HashMap<>();

    void read(Object table, Object key) {
        reads.computeIfAbsent(table, ignored -> new HashSet<>()).add(key);
    }

    void readWholeTable(Object table) {
        read(table, WHOLE_TABLE);
    }

    void write(Object table, Object key) {
        writes.computeIfAbsent(table, ignored -> new HashSet<>()).add(key);
    }

    /** Whether anything this trial wrote was read by {@code reader}'s trial. */
    boolean mayChangeWhatIsReadBy(DependencyFootprint reader) {
        for (Map.Entry<Object, Set<Object>> written : writes.entrySet()) {
            Set<Object> readKeys = reader.reads.get(written.getKey());
            if (readKeys == null) {
                continue;
            }
            if (readKeys.contains(WHOLE_TABLE)) {
                return true;
            }
            for (Object key : written.getValue()) {
                if (readKeys.contains(key)) {
                    return true;
                }
            }
        }
        return false;
    }
}
