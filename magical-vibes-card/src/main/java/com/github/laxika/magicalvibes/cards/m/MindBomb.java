package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDiscardUpToThenTakeDamageEffect;

@CardRegistration(set = "5ED", collectorNumber = "105")
@CardRegistration(set = "4ED", collectorNumber = "87")
public class MindBomb extends Card {

    public MindBomb() {
        addEffect(EffectSlot.SPELL, new EachPlayerMayDiscardUpToThenTakeDamageEffect(3));
    }
}
