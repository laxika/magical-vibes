package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostXPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BFZ", collectorNumber = "77")
public class ExertInfluence extends Card {

    public ExertInfluence() {
        target(TargetFilters.creature())
                .addSpellEffect(new ConditionalEffect(
                        new TargetPermanentMatches(new PermanentPowerAtMostXPredicate()),
                        new GainControlOfTargetEffect(Duration.PERMANENT)));
    }
}
