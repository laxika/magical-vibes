package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "129")
public class KozileksSentinel extends Card {

    public KozileksSentinel() {
        // Whenever you cast a colorless spell, this creature gets +1/+0 until end of turn.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardIsColorlessPredicate(),
                List.of(new BoostSelfEffect(1, 0))
        ));
    }
}
