package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "MH1", collectorNumber = "45")
public class CunningEvasion extends Card {

    public CunningEvasion() {
        // Whenever a creature you control becomes blocked, you may return it to its owner's hand.
        addEffect(EffectSlot.ON_ALLY_CREATURE_BECOMES_BLOCKED,
                new MayEffect(ReturnToHandEffect.self(), "Return it to its owner's hand?"));
    }
}
