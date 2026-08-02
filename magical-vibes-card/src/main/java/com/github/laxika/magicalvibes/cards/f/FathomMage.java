package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "GTC", collectorNumber = "162")
public class FathomMage extends Card {

    public FathomMage() {
        // Evolve is auto-loaded as Keyword.EVOLVE; the ally-creature entry scan drives it.

        // Whenever one or more +1/+1 counters are put on Fathom Mage, you may draw a card.
        addEffect(EffectSlot.ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT,
                new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
