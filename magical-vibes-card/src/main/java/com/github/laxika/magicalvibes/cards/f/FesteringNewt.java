package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.FixedIfControlsAllNamed;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "M14", collectorNumber = "98")
public class FesteringNewt extends Card {

    private static final String BOGBREW_WITCH = "Bogbrew Witch";

    public FesteringNewt() {
        // When this creature dies, target creature an opponent controls gets -1/-1 until end of
        // turn. That creature gets -4/-4 instead if you control a creature named Bogbrew Witch.
        // The -1/-1 vs -4/-4 choice is a resolution-time amount, so a single boost effect covers
        // both branches and only one target is chosen.
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                )),
                "Target must be a creature an opponent controls"
        ))
                .addEffect(EffectSlot.ON_DEATH, new BoostTargetCreatureEffect(
                        new FixedIfControlsAllNamed(List.of(BOGBREW_WITCH), -4, -1),
                        new FixedIfControlsAllNamed(List.of(BOGBREW_WITCH), -4, -1)));
    }
}
