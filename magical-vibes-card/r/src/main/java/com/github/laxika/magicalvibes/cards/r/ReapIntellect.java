package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "95")
public class ReapIntellect extends Card {

    public ReapIntellect() {
        // Target opponent reveals their hand; choose up to X nonland cards and exile them. For each
        // exiled card, the caster searches that player's graveyard, hand, and library for any number of
        // cards with the same name and exiles them. The shuffle is its own final step: it happens even
        // when no card is chosen.
        addSpellEffect(new ChooseCardsFromTargetHandEffect(
                new XValue(), List.of(), List.of(), HandChoiceDestination.EXILE, false,
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)), true, true));
        addSpellEffect(new ShuffleLibraryEffect(true));
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Target must be an opponent"));
    }
}
