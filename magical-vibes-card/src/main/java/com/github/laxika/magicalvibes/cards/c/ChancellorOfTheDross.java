package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "NPH", collectorNumber = "54")
public class ChancellorOfTheDross extends Card {

    public ChancellorOfTheDross() {
        addEffect(EffectSlot.ON_OPENING_HAND_REVEAL, new MayEffect(
                new LoseLifeEffect(3, LoseLifeRecipient.EACH_OPPONENT, true),
                "Reveal this card from your opening hand?"
        ));
    }
}
