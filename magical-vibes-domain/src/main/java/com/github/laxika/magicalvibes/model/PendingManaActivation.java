package com.github.laxika.magicalvibes.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

/**
 * A mana activation that has tapped its source but not yet produced its mana, because it stopped to
 * ask the player which colour to add ("{T}: Add one mana of any colour").
 *
 * <p>{@link ManaActivation} is recorded by diffing the pool around resolution, which for these
 * abilities would measure nothing: the prompt opens during resolution and the mana only arrives when
 * the answer comes back, long after the activation call returned. Without somewhere to park the
 * before-snapshot, such an activation could not be made revertable at all, and tapping a Birds of
 * Paradise to help pay for a spell used to poison the whole cancel window — the bird stayed tapped
 * and its mana stayed floating.
 *
 * <p>So the activation parks itself here, and the colour-choice answer completes it into a real
 * {@link ManaActivation}. Restricted mana choices also park their total-mana snapshot so
 * land-tap triggers can observe the mana produced, but leave {@code poolBefore} null to
 * prevent reverting an activation whose restricted buckets cannot be safely drained.
 *
 * @param playerId          the player who activated the mana ability
 * @param permanentId       the tapped source permanent
 * @param poolBefore        per-colour plain pool immediately before resolution
 * @param creatureManaBefore per-colour creature-mana pool immediately before resolution
 * @param deferredTriggers  triggers this activation deferred into
 *                          {@link GameData#pendingManaAbilityTriggers}
 * @param allManaBefore     per-colour totals across all mana buckets before resolution
 * @param standalone        the activation was made with priority and no payment in progress, so
 *                          once its colour is chosen its controller receives priority and its
 *                          non-mana triggers go on the stack instead of being held revertable
 */
public record PendingManaActivation(
        UUID playerId,
        UUID permanentId,
        EnumMap<ManaColor, Integer> poolBefore,
        EnumMap<ManaColor, Integer> creatureManaBefore,
        List<StackEntry> deferredTriggers,
        EnumMap<ManaColor, Integer> allManaBefore,
        boolean standalone
) {
    public PendingManaActivation(UUID playerId, UUID permanentId,
                                 EnumMap<ManaColor, Integer> poolBefore,
                                 EnumMap<ManaColor, Integer> creatureManaBefore,
                                 List<StackEntry> deferredTriggers,
                                 EnumMap<ManaColor, Integer> allManaBefore) {
        this(playerId, permanentId, poolBefore, creatureManaBefore, deferredTriggers, allManaBefore, false);
    }

    public PendingManaActivation(UUID playerId, UUID permanentId,
                                 EnumMap<ManaColor, Integer> poolBefore,
                                 EnumMap<ManaColor, Integer> creatureManaBefore,
                                 List<StackEntry> deferredTriggers) {
        this(playerId, permanentId, poolBefore, creatureManaBefore, deferredTriggers, poolBefore, false);
    }

    /** This activation marked as made on its own, with no payment in progress. */
    public PendingManaActivation asStandalone() {
        return new PendingManaActivation(playerId, permanentId, poolBefore, creatureManaBefore,
                deferredTriggers, allManaBefore, true);
    }

    /** This activation with {@code moreTriggers} also deferred by it. */
    public PendingManaActivation withMoreDeferredTriggers(List<StackEntry> moreTriggers) {
        if (moreTriggers.isEmpty()) {
            return this;
        }
        List<StackEntry> all = new ArrayList<>(deferredTriggers);
        all.addAll(moreTriggers);
        return new PendingManaActivation(playerId, permanentId, poolBefore, creatureManaBefore,
                List.copyOf(all), allManaBefore, standalone);
    }
}
