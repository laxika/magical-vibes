package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfSubjectEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C20", collectorNumber = "31")
public class CrystallineResonance extends Card {

    public CrystallineResonance() {
        BecomeCopyOfSubjectEffect copyEffect = BecomeCopyOfSubjectEffect.of(Subjects.target())
                .lasting(Duration.UNTIL_YOUR_NEXT_TURN)
                .keepingThisAbility();
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                "Target must be another permanent.")).addTrigger(Triggers.youCycleCard(),
                new MayEffect(
                        copyEffect,
                        "Have Crystalline Resonance become a copy of that permanent until your next turn?"));
    }
}
