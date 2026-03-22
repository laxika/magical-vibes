package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToXValueEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

/**
 * Temple of Aclazotz — back face of Arguel's Blood Fast.
 * Legendary Land.
 * {T}: Add {B}.
 * {T}, Sacrifice a creature: You gain life equal to the sacrificed creature's toughness.
 */
public class TempleOfAclazotz extends Card {

    public TempleOfAclazotz() {
        // {T}: Add {B}.
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.BLACK));

        // {T}, Sacrifice a creature: You gain life equal to the sacrificed creature's toughness.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new SacrificeCreatureCost(false, false, true), new GainLifeEqualToXValueEffect()),
                "{T}, Sacrifice a creature: You gain life equal to the sacrificed creature's toughness.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature you control"
                )
        ));
    }
}
