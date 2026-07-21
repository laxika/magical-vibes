package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.Chance;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

/**
 * Leave // Chance — front half (Leave).
 * Instant — Return any number of target permanents you own to your hand.
 * Back half (Chance) is cast only from the graveyard via Aftermath (FlashbackCast on the back face).
 */
@CardRegistration(set = "HOU", collectorNumber = "153")
public class LeaveChance extends Card {

    public LeaveChance() {
        Chance chance = new Chance();
        chance.setSetCode(getSetCode());
        chance.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(chance);

        // Return any number of target permanents you own to your hand.
        target(new OwnedPermanentPredicateTargetFilter(
                new PermanentTruePredicate(),
                "Target must be a permanent you own"
        ), 0, 99).addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }

    @Override
    public String getBackFaceClassName() {
        return "Chance";
    }
}
