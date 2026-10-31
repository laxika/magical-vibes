package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfEachPlayersLibraryEffect;

@CardRegistration(set = "SNC", collectorNumber = "37")
public class CaseTheJoint extends Card {

    public CaseTheJoint() {
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new LookAtTopCardOfEachPlayersLibraryEffect());
    }
}
