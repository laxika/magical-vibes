package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "105")
@CardRegistration(set = "WHO", collectorNumber = "397")
@CardRegistration(set = "WHO", collectorNumber = "710")
@CardRegistration(set = "WHO", collectorNumber = "988")
public class JamieMcCrimmon extends Card {

    public JamieMcCrimmon() {
        // Whenever you cast a historic spell, Jamie McCrimmon gets +X/+X until end of turn,
        // where X is that spell's mana value.
        addTrigger(Triggers.youCastSpell().withFilter(new CardIsHistoricPredicate()),
                new BoostSelfEffect(new TargetSpellManaValue(), new TargetSpellManaValue()));
    }
}
