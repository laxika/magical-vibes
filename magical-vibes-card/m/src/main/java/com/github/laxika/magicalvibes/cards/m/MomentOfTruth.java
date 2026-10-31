package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsHandTopBottomEffect;

@CardRegistration(set = "MOM", collectorNumber = "67")
public class MomentOfTruth extends Card {

    public MomentOfTruth() {
        addSpellEffect(new LookAtTopCardsHandTopBottomEffect(3, true));
    }
}
