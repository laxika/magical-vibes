package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "C20", collectorNumber = "57")
public class SurlyBadgersaur extends Card {

    public SurlyBadgersaur() {
        addTrigger(Triggers.youDiscardCard(), new TriggeringCardConditionalEffect(
                new CardTypePredicate(CardType.CREATURE),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));
        addTrigger(Triggers.youDiscardCard(), new TriggeringCardConditionalEffect(
                new CardTypePredicate(CardType.LAND),
                CreateTokenEffect.ofTreasureToken(1)));

        target(TargetFilters.creatureAnOpponentControls(), 0, 1)
                .addTrigger(Triggers.youDiscardCard(), new TriggeringCardConditionalEffect(
                        new CardAllOfPredicate(List.of(
                                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND)))),
                        new SourceFightsTargetCreatureEffect()));
    }
}
