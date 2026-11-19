package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * "{Player} gets N rad counters." The amount is evaluated once, in the resolving entry's context (with the defending
 * player as the amount's target for {@code defendingPlayer()}); nothing happens if it is not positive. Every player the
 * subject names then gets that many, after counter replacements (Vorinclex, Monstrous Raider) and "players can't get
 * counters", one placement per player in APNAP order, each a "you put counters on a player" event.
 *
 * @param amount number of rad counters to give
 * @param player who gets them; only {@code targetPlayer()} declares a (benign) player target
 */
public record GiveRadCountersEffect(DynamicAmount amount, PlayerSubject player) implements CardEffect {

    public GiveRadCountersEffect(int amount, PlayerSubject player) {
        this(new Fixed(amount), player);
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget()
                ? TargetSpec.benign(TargetPredicates.player())
                : TargetSpec.NONE;
    }

    @Override
    public boolean referencesEventValue() {
        return amount instanceof EventValue;
    }
}
