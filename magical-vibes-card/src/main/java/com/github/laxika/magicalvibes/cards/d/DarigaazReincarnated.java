package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileWithEggCountersInsteadOfDyingEffect;

@CardRegistration(set = "DOM", collectorNumber = "193")
public class DarigaazReincarnated extends Card {

    public DarigaazReincarnated() {
        // Flying, trample, haste — loaded from Scryfall
        // "If Darigaaz Reincarnated would die, instead exile it with three egg counters on it."
        addEffect(EffectSlot.STATIC, new ExileWithEggCountersInsteadOfDyingEffect(3));
        // "At the beginning of your upkeep, if this card is exiled with an egg counter on it,
        //  remove an egg counter from it. Then if this card has no egg counters on it, return
        //  it to the battlefield."
        // Upkeep trigger is handled by StepTriggerService scanning exiledCardEggCounters.
    }
}
