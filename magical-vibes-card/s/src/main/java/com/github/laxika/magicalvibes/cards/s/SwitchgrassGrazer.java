package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.condition.SourceIsSaddled;
import com.github.laxika.magicalvibes.model.effect.BecomeSaddledUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageNotRemovedDuringCleanupEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SaddleCost;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YOTJ", collectorNumber = "14")
public class SwitchgrassGrazer extends Card {

    public SwitchgrassGrazer() {
        // The damage is dealt on every attack; only the perpetual rider asks whether it is saddled (a plain "if" read
        // as the ability resolves, not an intervening "if").
        addTrigger(Triggers.attacks(),
                DealDamageEffect.toAnyTarget(1),
                new ConditionalEffect(new SourceIsSaddled(), new PerpetuallyGrantAbilitiesEffect(
                        Subjects.cardsOf(Subjects.thisWay(StepKind.DEAL_DAMAGE, new PermanentIsCreaturePredicate())),
                        List.of(StaticAbility.of(new CantBlockEffect()),
                                StaticAbility.of(new DamageNotRemovedDuringCleanupEffect())))));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SaddleCost(1), new BecomeSaddledUntilEndOfTurnEffect(Subjects.source())),
                "Saddle 1",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
