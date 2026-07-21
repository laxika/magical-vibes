package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.effect.ControlTargetPlayerNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.Set;

@CardRegistration(set = "INR", collectorNumber = "5")
public class EmrakulThePromisedEnd extends Card {

    public EmrakulThePromisedEnd() {
        // This spell costs {1} less to cast for each card type among cards in your graveyard.
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(new CardTypesAmongCardsInGraveyard()));

        // When you cast this spell, you gain control of target opponent during that player's next
        // turn. After that turn, that player takes an extra turn.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.ON_SELF_CAST, new ControlTargetPlayerNextTurnEffect(true));

        // Flying, trample from Scryfall keywords; protection from instants is typed protection.
        addEffect(EffectSlot.STATIC, new ProtectionFromCardTypesEffect(Set.of(CardType.INSTANT)));
    }
}
