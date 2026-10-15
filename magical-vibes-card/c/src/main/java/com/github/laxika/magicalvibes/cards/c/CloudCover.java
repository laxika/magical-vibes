package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "PLS", collectorNumber = "98")
public class CloudCover extends Card {

    public CloudCover() {
        addEffect(EffectSlot.ON_ANOTHER_ALLY_PERMANENT_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
                new MayEffect(ReturnToHandEffect.triggering(),
                        "Return that permanent to its owner's hand?"));
    }
}
