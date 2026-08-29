package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "187")
public class PawpatchRecruit extends Card {

    public PawpatchRecruit() {
        PermanentPredicate anotherCreatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));

        addEffect(EffectSlot.STATIC, new KickerEffect("{2}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new Kicked(),
                new CreateTokenCopyOfSourceEffect(false, 1, null, null, false, 1, 1)));
        target(new ControlledPermanentPredicateTargetFilter(
                anotherCreatureYouControl,
                "Target must be another creature you control"
        )).addEffect(EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
                PutCounterOnTargetPermanentEffect.withTargetRestriction(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, anotherCreatureYouControl));
    }
}
