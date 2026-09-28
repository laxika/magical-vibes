package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfChosenCreatureTypeToHandRestRandomEffect;

@CardRegistration(set = "CMM", collectorNumber = "740")
@CardRegistration(set = "CMM", collectorNumber = "770")
public class ForTheAncestors extends Card {

    public ForTheAncestors() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsOfChosenCreatureTypeToHandRestRandomEffect(6));
    }
}
