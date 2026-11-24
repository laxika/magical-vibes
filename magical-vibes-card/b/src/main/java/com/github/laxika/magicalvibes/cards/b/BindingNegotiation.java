package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.PutFaceUpExiledCardOwnedByTargetIntoGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "78")
public class BindingNegotiation extends Card {

    public BindingNegotiation() {
        // Target opponent reveals their hand. You may choose a nonland card from it. If you do, they discard it.
        // Otherwise, you may put a face-up exiled card they own into their graveyard.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addSpellEffect(new ChooseCardsFromTargetHandEffect(new Fixed(1), List.of(CardType.LAND), List.of(),
                HandChoiceDestination.DISCARD, false, null, 0, true, false, true, false, false, 0,
                new PutFaceUpExiledCardOwnedByTargetIntoGraveyardEffect(), 0, false));
    }
}
