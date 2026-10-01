package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentCreatureAndPerpetuallyBoostEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasLeastToughnessAmongOpponentCreaturesPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "YBRO", collectorNumber = "7")
public class PenregonBesieged extends Card {

    public PenregonBesieged() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ChooseOpponentCreatureAndPerpetuallyBoostEffect(
                        -1, -1, new PermanentHasLeastToughnessAmongOpponentCreaturesPredicate()));

        addEffect(EffectSlot.STATE_TRIGGERED, StateTriggerEffect.whenBattlefieldHasAtMost(0,
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                        new PermanentIsCreaturePredicate())),
                List.of(new SacrificeSelfEffect()),
                "Penregon Besieged's state-triggered ability"));
    }
}
