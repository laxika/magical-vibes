package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONE", collectorNumber = "30")
public class PorcelainZealot extends Card {

    public PorcelainZealot() {
        target(TargetFilters.creatureYouControl()).addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new ConditionalReplacementEffect(
                        new TargetPermanentMatches(new PermanentHasKeywordPredicate(Keyword.TOXIC)),
                        BoostEffect.target(1, 1),
                        BoostEffect.target(2, 2)));
    }
}
