package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerIsController;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C19", collectorNumber = "14")
public class ArchfiendOfSpite extends Card {

    public ArchfiendOfSpite() {
        // Whenever a source an opponent controls deals damage to this creature, that source's controller loses that
        // much life unless they sacrifice that many permanents of their choice.
        addTrigger(Triggers.dealtDamage(),
                TriggerOptions.EACH_SOURCE.withEventCondition(new NotCondition(new TargetPlayerIsController())),
                new MayEffect(
                        new SacrificePermanentsEffect(new EventValue(), new PermanentTruePredicate(),
                                Subjects.controllerOf(Subjects.triggering())),
                        "Sacrifice that many permanents? If you don't, you lose that much life.",
                        new LoseLifeEffect(new EventValue(), Subjects.controllerOf(Subjects.triggering())),
                        MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER));
        addCastingOption(new MadnessCast("{3}{B}{B}"));
    }
}
