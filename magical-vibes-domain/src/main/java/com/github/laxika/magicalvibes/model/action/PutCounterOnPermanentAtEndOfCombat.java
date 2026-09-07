package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.UUID;

/**
 * Permanent scheduled to receive {@code amount} counters of {@code counterType} when combat ends
 * (e.g. Greater Werewolf's "At end of combat, put a -0/-2 counter on each creature blocking or
 * blocked by this creature"). Unlike {@link PutMinusOneCounterAtEndOfCombat} (which always targets
 * the source with -1/-1 counters), this carries an arbitrary target permanent and counter type.
 * When {@code alsoTap} is true the permanent is also tapped (Dread Wight).
 * <p>
 * {@code tokenForController}, when non-null, is created for the permanent's controller as part of
 * the same end-of-combat action — Kjeldoran Home Guard's "put a -0/-1 counter on this creature and
 * create a 0/1 white Deserter creature token". It is created even when the counter itself cannot be
 * placed, since one trigger does both. Drained in
 * {@code CombatService.processEndOfCombatOpponentCounters()}.
 */
public record PutCounterOnPermanentAtEndOfCombat(
        UUID permanentId,
        CounterType counterType,
        int amount,
        boolean alsoTap,
        CreateTokenEffect tokenForController,
        UUID requiredSourcePermanentId
) implements DelayedAction {

    /** Counter only (Greater Werewolf). */
    public PutCounterOnPermanentAtEndOfCombat(UUID permanentId, CounterType counterType, int amount) {
        this(permanentId, counterType, amount, false, null, null);
    }

    /** Counter plus optional tap (Dread Wight). */
    public PutCounterOnPermanentAtEndOfCombat(UUID permanentId, CounterType counterType, int amount,
                                               boolean alsoTap, UUID requiredSourcePermanentId) {
        this(permanentId, counterType, amount, alsoTap, null, requiredSourcePermanentId);
    }

    public PutCounterOnPermanentAtEndOfCombat(UUID permanentId, CounterType counterType, int amount,
                                               boolean alsoTap, CreateTokenEffect tokenForController) {
        this(permanentId, counterType, amount, alsoTap, tokenForController, null);
    }
}
