package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostXWhenMadnessOtherwisePredicate;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "96")
public class WelcomeToTheFold extends Card {

    public WelcomeToTheFold() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentToughnessAtMostXWhenMadnessOtherwisePredicate(2)
                )),
                "Target must be a creature with toughness 2 or less, or X or less if cast for madness"
        )).addEffect(EffectSlot.SPELL, new GainControlOfTargetEffect(ControlDuration.PERMANENT));

        addCastingOption(new MadnessCast("{X}{U}{U}"));
    }
}
