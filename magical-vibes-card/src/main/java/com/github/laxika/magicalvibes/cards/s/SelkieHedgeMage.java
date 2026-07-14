package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "158")
public class SelkieHedgeMage extends Card {

    public SelkieHedgeMage() {
        // The bounce trigger below targets a tapped creature; the card-level filter narrows the
        // deferred ETB target choice (CR 603.3d) to tapped creatures only.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsTappedPredicate())),
                "Target must be a tapped creature"));

        // When this creature enters, if you control two or more Forests, you may gain 3 life.
        // Intervening-if gate (CR 603.4): checked as the trigger goes on the stack and again at
        // resolution; non-targeting, the "you may" is decided at resolution.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new ControlsPermanentCount(2, new PermanentHasSubtypePredicate(CardSubtype.FOREST)),
                new MayEffect(new GainLifeEffect(3), "Gain 3 life?")));

        // When this creature enters, if you control two or more Islands, you may return target tapped
        // creature to its owner's hand. The gate defers targeting to trigger time (CR 603.3d); the
        // "you may" is decided at resolution.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new ControlsPermanentCount(2, new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                new MayEffect(ReturnToHandEffect.target(),
                        "Return target tapped creature to its owner's hand?")));
    }
}
