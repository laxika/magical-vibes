package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EMA", collectorNumber = "96")
@CardRegistration(set = "C14", collectorNumber = "25")
public class MaliciousAffliction extends Card {

    public MaliciousAffliction() {
        // Morbid: "When you cast this spell, if a creature died this turn": the intervening "if".
        addTrigger(Triggers.youCastThisSpell(),
                TriggerOptions.DEFAULT.withInterveningIf(new Morbid()),
                new MayEffect(new CopyTriggeringSpellEffect(), "Copy Malicious Affliction?"));

        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK)))
                )),
                "Target must be a nonblack creature"
        )).addSpellEffect(new DestroyTargetPermanentEffect());
    }
}
