package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsHandTopBottomEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "MOM", collectorNumber = "67")
public class MomentOfTruth extends Card {

    public MomentOfTruth() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsHandTopBottomEffect(3));
        addEffect(EffectSlot.SPELL, new MillEffect(1, MillRecipient.CONTROLLER));
    }
}
