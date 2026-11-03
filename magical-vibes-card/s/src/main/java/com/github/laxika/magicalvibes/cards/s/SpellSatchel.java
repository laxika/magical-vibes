package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "258")
public class SpellSatchel extends Card {

    public SpellSatchel() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        List<CardEffect> addBookCounter =
                List.of(new PutCountersOnSelfEffect(CounterType.BOOK));

        addTrigger(Triggers.youCastSpell().withFilter(instantOrSorcery), addBookCounter.toArray(CardEffect[]::new));
        addTrigger(Triggers.youCopySpell().withFilter(instantOrSorcery), addBookCounter.toArray(CardEffect[]::new));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.BOOK),
                        new AwardManaEffect(ManaColor.COLORLESS)
                ),
                "{T}, Remove a book counter from this artifact: Add {C}."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new RemoveCounterFromSourceCost(3, CounterType.BOOK),
                        new DrawCardEffect(1)
                ),
                "{3}, {T}, Remove three book counters from this artifact: Draw a card."
        ));
    }
}
