package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import java.util.List;
import java.util.UUID;

/**
 * Cast trigger that offers each player a life payment and counters the triggering spell when a
 * player pays. The remaining players still receive their choices after a payment.
 */
public record AnyPlayerMayPayLifeToCounterSpellEffect(
        DynamicAmount lifeCost,
        List<UUID> remainingPlayerIds,
        UUID abilityControllerId,
        UUID targetCardId
) implements TriggeringSpellReferencingEffect, CounterSpellingEffect {

    public AnyPlayerMayPayLifeToCounterSpellEffect(int lifeCost) {
        this(new Fixed(lifeCost));
    }

    public AnyPlayerMayPayLifeToCounterSpellEffect(DynamicAmount lifeCost) {
        this(lifeCost, List.of(), null, null);
    }

    public AnyPlayerMayPayLifeToCounterSpellEffect {
        remainingPlayerIds = remainingPlayerIds == null ? List.of() : List.copyOf(remainingPlayerIds);
    }
}
