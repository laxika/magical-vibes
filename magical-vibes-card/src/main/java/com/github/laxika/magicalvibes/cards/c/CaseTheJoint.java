package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfEachPlayersLibraryEffect;

@CardRegistration(set = "SNC", collectorNumber = "37")
public class CaseTheJoint extends Card {

    public CaseTheJoint() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
        addEffect(EffectSlot.SPELL, new LookAtTopCardOfEachPlayersLibraryEffect());
    }
}
