package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "132")
@CardRegistration(set = "FDC", collectorNumber = "151")
public class DragonhawkFatesTempest extends Card {

    public DragonhawkFatesTempest() {
        var powerFourCreatures = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentPowerAtLeastPredicate(4)
        ));
        var effect = SequenceEffect.of(
                new ExileTopCardMayPlayThisTurnEffect(
                        new PermanentCount(powerFourCreatures, CountScope.CONTROLLER), false)
                        .until(Duration.UNTIL_YOUR_NEXT_END_STEP),
                RegisterDelayedStepTriggerEffect.aboutExiled(Triggers.endStep(ActivePlayer.YOU),
                                DealDamageEffect.to(Subjects.eachOpponent(),
                                        new Scaled(AmountThisWay.stillExiled(), 2)))
                        .evenIfNothingHappened());
        addTrigger(Triggers.enters(ObjectRelation.THIS), effect);
        addTrigger(Triggers.attacks(), effect);
    }
}
