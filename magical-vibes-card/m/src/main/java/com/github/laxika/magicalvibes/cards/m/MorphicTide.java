package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MorphicTideEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OPC2", collectorNumber = "3")
@CardRegistration(set = "OPCA", collectorNumber = "3")
public class MorphicTide extends Card {

    public MorphicTide() {
        // Every player's artifacts, creatures, lands and planeswalkers enter together; then every player's enchantments,
        // Auras and non-Auras together, each Aura enchanting something already there (the card's rulings).
        addTrigger(Triggers.encounterThis(), SequenceEffect.of(
                new MorphicTideEffect(Subjects.eachPlayer()),
                new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT), new CardTypePredicate(CardType.CREATURE),
                        new CardTypePredicate(CardType.LAND), new CardTypePredicate(CardType.PLANESWALKER))))),
                new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(new CardTypePredicate(CardType.ENCHANTMENT))),
                PutCardsIntoLibraryEffect.onBottom(Subjects.revealedThisWay())));
    }
}
