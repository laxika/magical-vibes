package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "MMQ", collectorNumber = "261")
public class Pangosaur extends Card {

    public Pangosaur() {
        // Whenever a player plays a land, return this creature to its owner's hand.
        addEffect(EffectSlot.ON_CONTROLLER_PLAYS_LAND, ReturnToHandEffect.self());
        addEffect(EffectSlot.ON_OPPONENT_PLAYS_LAND, ReturnToHandEffect.self());
    }
}
