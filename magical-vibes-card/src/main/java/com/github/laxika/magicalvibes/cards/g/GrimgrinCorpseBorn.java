package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.UntapSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "214")
public class GrimgrinCorpseBorn extends Card {

    public GrimgrinCorpseBorn() {
        // Grimgrin enters tapped
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // Doesn't untap during your untap step
        addEffect(EffectSlot.STATIC, new DoesntUntapDuringUntapStepEffect());

        // Sacrifice another creature: Untap Grimgrin and put a +1/+1 counter on it
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new UntapSelfEffect(),
                        new PutCountersOnSourceEffect(1, 1, 1)
                ),
                "Sacrifice another creature: Untap Grimgrin and put a +1/+1 counter on it."
        ));

        // Whenever Grimgrin attacks, destroy target creature defending player controls,
        // then put a +1/+1 counter on Grimgrin
        setTargetFilter(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                )),
                "Target must be a creature defending player controls"
        ));
        addEffect(EffectSlot.ON_ATTACK, new DestroyTargetPermanentEffect());
        addEffect(EffectSlot.ON_ATTACK, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
