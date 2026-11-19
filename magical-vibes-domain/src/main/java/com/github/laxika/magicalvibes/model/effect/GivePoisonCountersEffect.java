package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * "{Player} gets N poison counters." The amount is evaluated once, in the resolving entry's context, and every player
 * the subject names gets that many (several players in APNAP order). Placement goes through the engine's one poison
 * path, so poison replacements, "can't get poison counters" and the "you put counters" triggers apply; losing at ten
 * poison counters is the state-based action (CR 704.5c), not this effect.
 *
 * @param amount number of poison counters to give
 * @param player who gets them; only {@code targetPlayer()} declares a (benign) player target
 */
public record GivePoisonCountersEffect(DynamicAmount amount, PlayerSubject player)
        implements CardEffect {

    public GivePoisonCountersEffect(int amount, PlayerSubject player) {
        this(new Fixed(amount), player);
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget()
                ? TargetSpec.benign(TargetPredicates.player())
                : TargetSpec.NONE;
    }
}
