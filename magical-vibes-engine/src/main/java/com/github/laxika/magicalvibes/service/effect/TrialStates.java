package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.layer.CharacteristicState;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The per-permanent states of one CR 613.8 dependency trial in {@link LayerSystemService}: a
 * {@link RecordingCharacteristicState} copy of each layer-entry state, made when the trial first
 * reads that permanent instead of for the whole battlefield up front. A trial applies one or two
 * effect instances, which reach a handful of permanents, while a layer with k instances runs
 * k² trials, so copying every state for each of them dominated large boards.
 *
 * <p>The map behaves as the eager copy did. The layer-entry states are not mutated while a trial
 * runs, so a copy made on first read equals one made at the start. {@link #get} and
 * {@link #containsKey} copy only the permanent asked for; any whole-map operation (iteration,
 * {@code put}, {@code remove}) first copies every remaining state and from then on works on
 * a plain map of all copies. The fingerprint is read from the copies, which hold every operation
 * the trial recorded even when a later {@code put} or {@code remove} replaced a map entry.</p>
 */
final class TrialStates extends AbstractMap<UUID, CharacteristicState> {

    private final Map<UUID, CharacteristicState> layerEntryStates;
    private final Map<UUID, RecordingCharacteristicState> copies = new HashMap<>();
    private Map<UUID, CharacteristicState> materialized;
    private boolean recording;

    TrialStates(Map<UUID, CharacteristicState> layerEntryStates) {
        this.layerEntryStates = layerEntryStates;
    }

    @Override
    public CharacteristicState get(Object key) {
        if (materialized != null) {
            return materialized.get(key);
        }
        RecordingCharacteristicState copy = copies.get(key);
        if (copy != null) {
            return copy;
        }
        CharacteristicState state = layerEntryStates.get(key);
        return state == null ? null : copy((UUID) key, state);
    }

    @Override
    public boolean containsKey(Object key) {
        return materialized != null ? materialized.containsKey(key) : layerEntryStates.containsKey(key);
    }

    @Override
    public int size() {
        return materialized != null ? materialized.size() : layerEntryStates.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public CharacteristicState put(UUID key, CharacteristicState value) {
        return materialize().put(key, value);
    }

    @Override
    public CharacteristicState remove(Object key) {
        return materialize().remove(key);
    }

    @Override
    public Set<Entry<UUID, CharacteristicState>> entrySet() {
        return materialize().entrySet();
    }

    /** Starts logging operations on every copy made so far and on each one made from now on. */
    void startRecording() {
        recording = true;
        copies.values().forEach(RecordingCharacteristicState::startRecording);
    }

    /** The operations each permanent's copy recorded, keyed by permanent; untouched permanents are absent. */
    Map<UUID, List<String>> fingerprint() {
        Map<UUID, List<String>> fingerprint = new HashMap<>();
        copies.forEach((id, copy) -> {
            if (!copy.ops().isEmpty()) {
                fingerprint.put(id, copy.ops());
            }
        });
        return fingerprint;
    }

    private RecordingCharacteristicState copy(UUID id, CharacteristicState state) {
        RecordingCharacteristicState copy = new RecordingCharacteristicState(state);
        if (recording) {
            copy.startRecording();
        }
        copies.put(id, copy);
        return copy;
    }

    private Map<UUID, CharacteristicState> materialize() {
        if (materialized == null) {
            layerEntryStates.forEach((id, state) -> {
                if (!copies.containsKey(id)) {
                    copy(id, state);
                }
            });
            materialized = new HashMap<>(copies);
        }
        return materialized;
    }
}
