package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ISD", collectorNumber = "29")
public class Rebuke extends Card {

    public Rebuke() {
        target(TargetFilters.attackingCreature()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
