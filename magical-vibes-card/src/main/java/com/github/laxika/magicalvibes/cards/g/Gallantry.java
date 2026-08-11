package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

/**
 * Gallantry — {1}{W} Instant.
 * Target blocking creature gets +4/+4 until end of turn.
 * Draw a card.
 */
@CardRegistration(set = "TMP", collectorNumber = "20")
@CardRegistration(set = "ODY", collectorNumber = "23")
public class Gallantry extends Card {

    public Gallantry() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsBlockingPredicate()
                )),
                "Target must be a blocking creature."
        ))
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(4, 4))
                .addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
