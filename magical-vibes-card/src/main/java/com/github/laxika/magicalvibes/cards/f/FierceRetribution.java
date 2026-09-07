package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "VOW", collectorNumber = "13")
public class FierceRetribution extends Card {

    public FierceRetribution() {
        addCastingOption(AlternateHandCast.cleave("{5}{W}", TargetFilters.creature()));
        target(TargetFilters.attackingCreature()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
