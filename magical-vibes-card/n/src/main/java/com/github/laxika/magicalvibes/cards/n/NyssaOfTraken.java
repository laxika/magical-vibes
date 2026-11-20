package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "51")
@CardRegistration(set = "WHO", collectorNumber = "366")
@CardRegistration(set = "WHO", collectorNumber = "656")
@CardRegistration(set = "WHO", collectorNumber = "957")
public class NyssaOfTraken extends Card {

    public NyssaOfTraken() {
        addStaticEffect(new NoMaximumHandSizeEffect());

        targetUpTo(new AmountThisWay(StepKind.SACRIFICE), TargetFilters.creature(), 1000)
                .addTrigger(Triggers.attacks(), SequenceEffect.of(
                        SacrificePermanentsEffect.anyNumber(new PermanentIsArtifactPredicate(), Subjects.you()),
                        new ConditionalEffect(
                                HappenedThisWay.sacrificed(),
                                new QueueReflexiveAbilityEffect(
                                        SequenceEffect.of(
                                                new TapPermanentsEffect(Subjects.target()),
                                                new DrawCardEffect(new AmountThisWay(StepKind.SACRIFICE)))))));
    }
}
