package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "SPE", collectorNumber = "12")
public class VenomEddieBrock extends Card {

    public VenomEddieBrock() {
        // ON_ANY_CREATURE_DIES excludes the source itself, satisfying "another creature".
        // Register the draw first so the engine resolves the counter before the draw, matching the
        // order of the printed ability.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.VILLAIN), new DrawCardEffect()));
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
