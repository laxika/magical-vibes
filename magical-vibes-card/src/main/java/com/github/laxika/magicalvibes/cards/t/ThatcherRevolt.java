package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "AVR", collectorNumber = "158")
public class ThatcherRevolt extends Card {

    public ThatcherRevolt() {
        // Create three 1/1 red Human creature tokens with haste.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(3, "Human", 1, 1, CardColor.RED,
                List.of(CardSubtype.HUMAN), Set.of(Keyword.HASTE), Set.<CardType>of()));

        // Sacrifice those tokens at the beginning of the next end step.
        addEffect(EffectSlot.SPELL, new SacrificeCreatedPermanentsAtEndStepEffect());
    }
}
