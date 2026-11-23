package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTJ", collectorNumber = "197")
public class BreechesTheBlastmaker extends Card {

    public BreechesTheBlastmaker() {
        addTrigger(Triggers.youCastSpell().nthThisTurn(2),
                new MayEffect(
                        SequenceEffect.of(
                                new SacrificePermanentsEffect(1, new PermanentIsArtifactPredicate(), Subjects.you()),
                                new ConditionalEffect(HappenedThisWay.sacrificed(),
                                        new FlipCoinWinEffect(
                                                new QueueReflexiveAbilityEffect(new CopyTriggeringSpellEffect()),
                                                new QueueReflexiveAbilityEffect(
                                                        new DealDamageToAnyTargetEffect(new TargetSpellManaValue()))))),
                        "Sacrifice an artifact?"));
    }
}
