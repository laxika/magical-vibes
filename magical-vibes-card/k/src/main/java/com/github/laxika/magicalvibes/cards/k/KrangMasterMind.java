package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtMost;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMT", collectorNumber = "43")
@CardRegistration(set = "TMT", collectorNumber = "230")
public class KrangMasterMind extends Card {

    public KrangMasterMind() {
        // Affinity for artifacts.
        addStaticEffect(new ReduceOwnCastCostEffect(
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER)));

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new CardsInHandAtMost(3)),
                new DrawCardEffect(new Sum(new Fixed(4), new Scaled(new CardsInHand(CountScope.CONTROLLER), -1))));

        PermanentCount otherArtifacts = new PermanentCount(
                new PermanentIsArtifactPredicate(), CountScope.CONTROLLER, true);
        addStaticEffect(new DynamicStaticBoostEffect(
                otherArtifacts, new Fixed(0), GrantScope.SELF));
    }
}
