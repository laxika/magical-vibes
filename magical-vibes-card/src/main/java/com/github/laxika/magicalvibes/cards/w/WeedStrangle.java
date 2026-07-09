package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TargetToughness;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "LRW", collectorNumber = "147")
public class WeedStrangle extends Card {

    public WeedStrangle() {
        // Destroy target creature. Clash with an opponent. If you win, you gain life equal to that
        // creature's toughness.
        //
        // The clash reward is listed before the destroy so that, on a clash win, the life gain
        // snapshots the creature's toughness while it is still on the battlefield (its last-known
        // toughness, per the official ruling). The two instructions are independent, so this
        // ordering is rules-equivalent.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                .addEffect(EffectSlot.SPELL, new ClashEffect(new GainLifeEffect(new TargetToughness())))
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
