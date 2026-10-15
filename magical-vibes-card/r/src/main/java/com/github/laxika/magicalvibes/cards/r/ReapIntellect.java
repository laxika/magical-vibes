package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "95")
public class ReapIntellect extends Card {

    public ReapIntellect() {
        // Target opponent reveals their hand; choose up to X nonland cards, exile them, then exile
        // every card with a chosen name from that player's graveyard, hand, and library and shuffle.
        addEffect(EffectSlot.SPELL, new ChooseCardsFromTargetHandEffect(
                new XValue(), List.of(), List.of(), HandChoiceDestination.EXILE, false,
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)), true, true));
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Target must be an opponent"));
    }
}
