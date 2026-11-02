package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "71")
@CardRegistration(set = "MKC", collectorNumber = "121")
@CardRegistration(set = "EOC", collectorNumber = "79")
@CardRegistration(set = "BRC", collectorNumber = "98")
@CardRegistration(set = "FDC", collectorNumber = "80")
public class ThoughtMonitor extends Card {

    public ThoughtMonitor() {
        addStaticEffect(new ReduceOwnCastCostEffect(
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER)));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(2));
    }
}
