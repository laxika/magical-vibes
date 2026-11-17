package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostXWhenMadnessOtherwisePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOI", collectorNumber = "96")
public class WelcomeToTheFold extends Card {

    public WelcomeToTheFold() {
        target(TargetFilters.creature())
                .addSpellEffect(new ConditionalEffect(
                        new TargetPermanentMatches(new PermanentToughnessAtMostXWhenMadnessOtherwisePredicate(2)),
                        new GainControlOfTargetEffect(Duration.PERMANENT)));

        addCastingOption(new MadnessCast("{X}{U}{U}"));
    }
}
