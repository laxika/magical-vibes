package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "118")
public class ThoughtStalkerWarlock extends Card {

    public ThoughtStalkerWarlock() {
        // When this creature enters, choose target opponent. If they lost life this turn, they
        // reveal their hand, you choose a nonland card from it, and they discard that card.
        // Otherwise, they discard a card.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalReplacementEffect(
                new TargetPlayerLostLifeThisTurn(),
                new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER),
                new ChooseCardsFromTargetHandEffect(1, List.of(CardType.LAND), HandChoiceDestination.DISCARD)
        ));
    }
}
