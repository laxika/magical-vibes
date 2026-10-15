package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtDamageToAnythingThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

/**
 * Restore the Peace — return each creature that dealt damage this turn to its owner's hand.
 */
@CardRegistration(set = "DGM", collectorNumber = "97")
public class RestoreThePeace extends Card {

    public RestoreThePeace() {
        addEffect(EffectSlot.SPELL, ReturnToHandEffect.allPermanentsMatching(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentDealtDamageToAnythingThisTurnPredicate()))));
    }
}
