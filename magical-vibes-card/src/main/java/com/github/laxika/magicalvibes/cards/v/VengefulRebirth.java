package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RecordReturnedGraveyardCardManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "62")
public class VengefulRebirth extends Card {

    public VengefulRebirth() {
        // Return target card from your graveyard to your hand (any card). The graveyard card is the
        // entry's targetId (targetZone GRAVEYARD); the any target below is group 0 (targetIds).
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .targetGraveyard(true)
                .build());

        // If you return a nonland card to your hand this way, record its mana value as the event
        // value. Unbound so the resolver keeps targetId on the returned graveyard card.
        addEffect(EffectSlot.SPELL, new RecordReturnedGraveyardCardManaValueEffect());

        // ... Vengeful Rebirth deals damage equal to that card's mana value to any target. Bound to
        // the any-target group (group 0) so the position accepts players and the AI reads it as
        // harmful damage; the amount is the event value recorded above (0 = no damage, e.g. a land).
        target(new AnyTargetPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate()
                )),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be any target"
        )).addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(new EventValue(), false, false, 0));

        // Exile Vengeful Rebirth.
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
