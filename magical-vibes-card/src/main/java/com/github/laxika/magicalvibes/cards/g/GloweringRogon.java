package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AmplifyEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "LGN", collectorNumber = "128")
public class GloweringRogon extends Card {

    public GloweringRogon() {
        // Amplify 1 — This creature enters with a +1/+1 counter on it for each Beast card
        // revealed from your hand.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AmplifyEffect(
                1, new CardSubtypePredicate(CardSubtype.BEAST)));
    }
}
