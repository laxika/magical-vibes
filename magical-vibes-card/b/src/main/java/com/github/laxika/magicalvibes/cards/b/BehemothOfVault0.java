package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TargetManaValue;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PIP", collectorNumber = "127")
@CardRegistration(set = "PIP", collectorNumber = "655")
public class BehemothOfVault0 extends Card {

    public BehemothOfVault0() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new EnergyCountersEffect(4));

        // "When you do, destroy that permanent": a reflexive ability with no target, about the dies trigger's target.
        target(TargetFilters.nonlandPermanent()).addTrigger(Triggers.dies(ObjectRelation.THIS),
                UnlessPaysEffect.youMayPay(new PayEnergyCost(new TargetManaValue()),
                                QueueReflexiveAbilityEffect.aboutDeclaredTarget(
                                        new DestroyTargetPermanentEffect(Subjects.triggering()),
                                        TargetSpec.harmful(TargetPredicates.permanents(
                                                new PermanentNotPredicate(new PermanentIsLandPredicate())))))
                        .withPrompt("Pay energy equal to that permanent's mana value to destroy it?"));
    }
}
