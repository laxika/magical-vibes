package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesCopyOfExiledCreatureWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "YLCI", collectorNumber = "27")
public class ReflectionNet extends Card {

    public ReflectionNet() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(), "Target must be a creature"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ExileTargetPermanentUntilSourceLeavesEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new TargetCreatureBecomesCopyOfExiledCreatureWithSourceEffect()),
                "{2}: Target creature you control becomes a copy of a creature card exiled with Reflection Net. "
                        + "Activate only as a sorcery and only once.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(), "Target must be a creature you control."),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ).withMaxActivationsPerGame(1));
    }
}
