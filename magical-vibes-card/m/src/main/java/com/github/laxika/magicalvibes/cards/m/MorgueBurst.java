package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.RecordReturnedGraveyardCardValueEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnedGraveyardCardValue;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "86")
public class MorgueBurst extends Card {

    public MorgueBurst() {
        // Return target creature card from your graveyard to your hand. The graveyard card is the
        // entry's targetId (targetZone GRAVEYARD); the any target below is group 0 (targetIds).
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build());

        // Record the returned card's power as the event value. Unbound so the resolver keeps
        // targetId on the returned graveyard card.
        addEffect(EffectSlot.SPELL, new RecordReturnedGraveyardCardValueEffect(ReturnedGraveyardCardValue.POWER));

        // Morgue Burst deals damage to any target equal to the power of the card returned this way.
        // Bound to the any-target group (group 0) so the position accepts players and the AI reads
        // it as harmful damage; a graveyard target that became illegal records 0 = no damage.
        target(new AnyTargetPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate()
                )),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be any target"
        )).addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(new EventValue(), false, false, 0));
    }
}
