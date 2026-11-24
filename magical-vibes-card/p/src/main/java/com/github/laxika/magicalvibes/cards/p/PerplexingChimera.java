package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseNewTargetsForTargetSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfPermanentAndSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BNG", collectorNumber = "48")
public class PerplexingChimera extends Card {

    public PerplexingChimera() {
        addTrigger(Triggers.opponentCastsSpell().anySpell(), SequenceEffect.of(
                new MayEffect(ExchangeControlOfPermanentAndSpellEffect.sourceAndTriggeringSpell(),
                        "Exchange control of this creature and that spell?"),
                new ConditionalEffect(new HappenedThisWay(StepKind.GAIN_CONTROL, 1),
                        new MayEffect(ChooseNewTargetsForTargetSpellEffect.ifItHasTargets(Subjects.triggeringSpell()),
                                "Choose new targets for the spell?"))));
    }
}
