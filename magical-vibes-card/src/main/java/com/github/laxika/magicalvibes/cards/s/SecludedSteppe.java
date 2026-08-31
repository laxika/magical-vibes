package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "ONS", collectorNumber = "324")
public class SecludedSteppe extends Card {

    public SecludedSteppe() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
        addCycling("{W}");
    }
}
