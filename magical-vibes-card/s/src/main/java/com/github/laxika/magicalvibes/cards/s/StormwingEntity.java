package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.ControllerCastSpellThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostByManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "73")
@CardRegistration(set = "M21", collectorNumber = "354")
public class StormwingEntity extends Card {

    public StormwingEntity() {
        // This spell costs {2}{U} less to cast if you've cast an instant or sorcery spell this turn.
        addStaticEffect(new ConditionalEffect(
                new ControllerCastSpellThisTurn(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)))),
                new ReduceOwnCastCostByManaCostEffect("{2}{U}")));


        // When this creature enters, scry 2.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ScryEffect(2));
    }
}
