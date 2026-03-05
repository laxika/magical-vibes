package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeAndControllerGainsLifeLostEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "NPH", collectorNumber = "54")
public class ChancellorOfTheDross extends Card {

    public ChancellorOfTheDross() {
        addEffect(EffectSlot.OPENING_HAND_TRIGGERED, new MayEffect(
                new EachOpponentLosesLifeAndControllerGainsLifeLostEffect(3),
                "Reveal this card from your opening hand?"
        ));
    }
}
