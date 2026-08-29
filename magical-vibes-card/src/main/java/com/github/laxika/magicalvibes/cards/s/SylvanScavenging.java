package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "113")
public class SylvanScavenging extends Card {

    public SylvanScavenging() {
        var creatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate()));
        var largeCreatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtLeastPredicate(4),
                new PermanentControlledBySourceControllerPredicate()));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Put a +1/+1 counter on target creature you control",
                        PutCounterOnTargetPermanentEffect.withTargetRestriction(
                                CounterType.PLUS_ONE_PLUS_ONE, 1, creatureYouControl)),
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 3/3 green Raccoon creature token if you control a creature with power 4 or greater",
                        new ConditionalEffect(
                                new ControlsPermanentCount(1, largeCreatureYouControl),
                                new CreateTokenEffect("Raccoon", 3, 3, CardColor.GREEN,
                                        List.of(CardSubtype.RACCOON), Set.of(), Set.of()))
                ))));
    }
}
