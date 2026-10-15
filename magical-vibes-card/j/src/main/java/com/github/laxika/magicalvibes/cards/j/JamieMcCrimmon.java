package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfByCastSpellManaValueEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;

@CardRegistration(set = "WHO", collectorNumber = "105")
@CardRegistration(set = "WHO", collectorNumber = "397")
@CardRegistration(set = "WHO", collectorNumber = "710")
@CardRegistration(set = "WHO", collectorNumber = "988")
public class JamieMcCrimmon extends Card {

    public JamieMcCrimmon() {
        // Whenever you cast a historic spell, Jamie McCrimmon gets +X/+X until end of turn,
        // where X is that spell's mana value.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new BoostSelfByCastSpellManaValueEffect(new CardIsHistoricPredicate()));
    }
}
