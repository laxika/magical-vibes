package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnEachAttackingCreatureEffect;

@CardRegistration(set = "MBS", collectorNumber = "4")
public class ChokingFumes extends Card {

    public ChokingFumes() {
        addEffect(EffectSlot.SPELL, new PutMinusOneMinusOneCounterOnEachAttackingCreatureEffect());
    }
}
