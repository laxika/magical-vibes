package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PCY", collectorNumber = "18")
public class ReveilleSquad extends Card {

    public ReveilleSquad() {
        addTrigger(Triggers.creaturesAttackYou(),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceUntapped()),
                new MayEffect(
                        new UntapPermanentsEffect(Subjects.controlledBy(Subjects.you(),
                                new PermanentIsCreaturePredicate())),
                        "Untap all creatures you control?"));
    }
}
