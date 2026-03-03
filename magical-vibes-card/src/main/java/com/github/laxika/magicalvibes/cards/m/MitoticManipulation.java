package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect;

@CardRegistration(set = "MBS", collectorNumber = "27")
public class MitoticManipulation extends Card {

    public MitoticManipulation() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsPutMatchingPermanentNameOnBattlefieldEffect(7));
    }
}
