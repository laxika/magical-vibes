package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.PutFaceUpExiledCardOwnedByTargetIntoGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasAdventurePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "96")
public class MemoryTheft extends Card {

    public MemoryTheft() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent."
        )).addEffect(EffectSlot.SPELL, new ChooseCardsFromTargetHandEffect(
                1, List.of(CardType.LAND), HandChoiceDestination.DISCARD))
                .addEffect(EffectSlot.SPELL,
                        new PutFaceUpExiledCardOwnedByTargetIntoGraveyardEffect(
                                new CardHasAdventurePredicate()));
    }
}
