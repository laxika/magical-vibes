package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "DGM", collectorNumber = "6")
public class RiotControl extends Card {

    public RiotControl() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.OPPONENTS)));
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allToController());
    }
}
