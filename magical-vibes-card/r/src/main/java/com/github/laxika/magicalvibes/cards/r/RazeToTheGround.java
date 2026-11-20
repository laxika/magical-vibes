package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BRO", collectorNumber = "149")
public class RazeToTheGround extends Card {

    public RazeToTheGround() {
        addStaticEffect(new CantBeCounteredEffect());
        target(TargetFilters.artifact())
                .addSpellEffect(new DestroyTargetPermanentEffect())
                        .addSpellEffect(new ConditionalEffect(new TargetPermanentMatches(new PermanentMaxManaValuePredicate(1)), new DrawCardEffect()));
    }
}
