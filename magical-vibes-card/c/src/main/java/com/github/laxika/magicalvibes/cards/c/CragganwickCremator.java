package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardDealDiscardedPowerToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "87")
@CardRegistration(set = "2XM", collectorNumber = "122")
public class CragganwickCremator extends Card {

    public CragganwickCremator() {
        // When this creature enters, discard a card at random. If you discard a creature card
        // this way, this creature deals damage equal to that card's power to target player or
        // planeswalker. The target is chosen when the ability triggers, regardless of the discard.
        target(new AnyTargetPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player or planeswalker."
        )).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DiscardRandomCardDealDiscardedPowerToTargetPlayerOrPlaneswalkerEffect());
    }
}
