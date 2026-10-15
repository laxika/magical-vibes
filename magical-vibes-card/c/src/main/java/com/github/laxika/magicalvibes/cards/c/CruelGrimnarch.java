package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsOrControllerGainsLifeEffect;

@CardRegistration(set = "ONE", collectorNumber = "88")
public class CruelGrimnarch extends Card {

    public CruelGrimnarch() {
        // When this creature enters, each opponent discards a card. For each opponent who can't,
        // you gain 4 life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EachOpponentDiscardsOrControllerGainsLifeEffect(4));
    }
}
