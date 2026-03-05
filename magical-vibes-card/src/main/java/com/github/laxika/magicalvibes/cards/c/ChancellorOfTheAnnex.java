package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCounterTriggerEffect;

@CardRegistration(set = "NPH", collectorNumber = "6")
public class ChancellorOfTheAnnex extends Card {

    public ChancellorOfTheAnnex() {
        // Whenever an opponent casts a spell, counter it unless that player pays {1}.
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new CounterUnlessPaysEffect(1));

        // You may reveal this card from your opening hand. If you do, when each opponent
        // casts their first spell of the game, counter that spell unless that player pays {1}.
        addEffect(EffectSlot.ON_OPENING_HAND_REVEAL, new MayEffect(
                new RegisterDelayedCounterTriggerEffect(1),
                "Reveal this card from your opening hand?"
        ));
    }
}
