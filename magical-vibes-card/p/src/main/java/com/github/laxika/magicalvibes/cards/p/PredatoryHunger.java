package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EXO", collectorNumber = "117")
public class PredatoryHunger extends Card {

    public PredatoryHunger() {
        target(TargetFilters.creature())
                // Whenever an opponent casts a creature spell, put a +1/+1 counter on enchanted creature.
                .addTrigger(Triggers.opponentCastsSpell().withFilter(new CardTypePredicate(CardType.CREATURE)),
                        new PutCountersEffect(Subjects.attached(), CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
