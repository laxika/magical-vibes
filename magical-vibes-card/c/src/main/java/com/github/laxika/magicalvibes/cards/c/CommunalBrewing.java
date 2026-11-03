package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CardsDrawnThisResolution;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalCountersToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLC", collectorNumber = "29")
@CardRegistration(set = "BLC", collectorNumber = "63")
public class CommunalBrewing extends Card {

    public CommunalBrewing() {
        PlayerPredicateTargetFilter opponent = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent");

        target(opponent, 0, 99).addTrigger(Triggers.enters(ObjectRelation.THIS),
                SequenceEffect.of(
                        new DrawCardForTargetPlayerEffect(1, false, true),
                        new PutCountersOnSelfEffect(CounterType.INGREDIENT),
                        new PutCountersOnSelfEffect(CounterType.INGREDIENT,
                                new CardsDrawnThisResolution())));

        addTrigger(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.CREATURE)),
                new GrantAdditionalCountersToCastSpellEffect(new CountersOnSource(CounterType.INGREDIENT)));
    }
}
