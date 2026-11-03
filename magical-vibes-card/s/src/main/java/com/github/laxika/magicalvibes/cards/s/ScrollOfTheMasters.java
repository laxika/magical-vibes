package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "163")
public class ScrollOfTheMasters extends Card {

    public ScrollOfTheMasters() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                new PutCountersOnSelfEffect(CounterType.LORE));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new BoostTargetCreatureEffect(
                        new CountersOnSource(CounterType.LORE),
                        new CountersOnSource(CounterType.LORE))),
                "{3}, {T}: Target creature you control gets +1/+1 until end of turn for each lore counter on Scroll of the Masters.",
                TargetFilters.creatureYouControl()
        ));
    }
}
