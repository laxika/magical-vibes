package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "39")
@CardRegistration(set = "DSC", collectorNumber = "66")
public class GigglingSkitterspike extends Card {

    public GigglingSkitterspike() {
        addTrigger(Triggers.attacks(),
                DealDamageEffect.to(Subjects.eachOpponent(), new SourcePower()));
        addTrigger(Triggers.blocks(),
                TriggerOptions.ONE_OR_MORE,
                DealDamageEffect.to(Subjects.eachOpponent(), new SourcePower()));
        addTrigger(Triggers.becomesTargetOfSpell(),
                DealDamageEffect.to(Subjects.eachOpponent(), new SourcePower()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(new MonstrosityEffect(5)),
                "{5}: Monstrosity 5."
        ));
    }
}
