package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedManaTriggerEffect;

@CardRegistration(set = "NPH", collectorNumber = "106")
public class ChancellorOfTheTangle extends Card {

    public ChancellorOfTheTangle() {
        // You may reveal this card from your opening hand. If you do,
        // at the beginning of your first main phase of the game, add {G}.
        addEffect(EffectSlot.ON_OPENING_HAND_REVEAL, new MayEffect(
                new RegisterDelayedManaTriggerEffect(ManaColor.GREEN, 1),
                "Reveal this card from your opening hand?"
        ));
    }
}
