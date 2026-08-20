package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlledCreaturesTotalToughnessAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesFractionOfLifeRoundedUpEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "TDM", collectorNumber = "172")
public class BetorKinToAll extends Card {

    public BetorKinToAll() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(new ControlledCreaturesTotalToughnessAtLeast(10),
                        SequenceEffect.of(
                                new DrawCardEffect(1),
                                ConditionalEffect.unless(
                                        new ControlledCreaturesTotalToughnessAtLeast(20),
                                        new UntapPermanentsEffect(TapUntapScope.CONTROLLED,
                                                new PermanentIsCreaturePredicate())),
                                ConditionalEffect.unless(
                                        new ControlledCreaturesTotalToughnessAtLeast(40),
                                        EachPlayerLosesFractionOfLifeRoundedUpEffect.opponentsOnly(2)))));
    }
}
