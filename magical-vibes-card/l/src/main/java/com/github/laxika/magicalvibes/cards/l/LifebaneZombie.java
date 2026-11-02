package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M14", collectorNumber = "101")
@CardRegistration(set = "PIO", collectorNumber = "95")
public class LifebaneZombie extends Card {

    public LifebaneZombie() {
        // When this creature enters, target opponent reveals their hand. You choose a green or
        // white creature card from it and exile that card.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ))
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new ChooseCardsFromTargetHandEffect(
                        new Fixed(1),
                        List.of(),
                        List.of(CardType.CREATURE),
                        HandChoiceDestination.EXILE,
                        false,
                        new CardAnyOfPredicate(List.of(
                                new CardColorPredicate(CardColor.GREEN),
                                new CardColorPredicate(CardColor.WHITE)))));
    }
}
