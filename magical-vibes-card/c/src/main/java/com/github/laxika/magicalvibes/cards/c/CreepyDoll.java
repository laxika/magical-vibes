package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;

@CardRegistration(set = "ISD", collectorNumber = "220")
public class CreepyDoll extends Card {

    public CreepyDoll() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE, new FlipCoinWinEffect(new DestroyTargetPermanentEffect()));
    }
}
