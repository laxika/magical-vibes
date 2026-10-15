package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantEscapeToGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WHO", collectorNumber = "600")
public class StormcageContainmentFacility extends Card {

    public StormcageContainmentFacility() {
        addEffect(EffectSlot.STATIC,
                new GrantEscapeToGraveyardCardsEffect(new CardTypePredicate(CardType.CREATURE)));
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.CHAOS_TRIGGERED, new LockTargetPermanentEffect(
                        true, true, true, EffectDuration.UNTIL_YOUR_NEXT_TURN));
    }
}
