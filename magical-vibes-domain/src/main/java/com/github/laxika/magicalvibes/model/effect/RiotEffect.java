package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.BattlefieldEntryRequest;

/**
 * Riot's as-enters choice: a creature enters with either a +1/+1 counter or haste.
 *
 * <p>This marker is placed in the static effect slot and resolved by the battlefield-entry and
 * may-ability services.
 */
public record RiotEffect(BattlefieldEntryRequest entryRequest, int remainingChoices,
                         int counterChoices, boolean hasteChosen) implements CardEffect {

    public RiotEffect() {
        this(null, 0, 0, false);
    }
}
