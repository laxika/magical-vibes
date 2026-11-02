package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OHOP", collectorNumber = "16")
@CardRegistration(set = "OPCA", collectorNumber = "36")
public class TheHippodrome extends Card {

    public TheHippodrome() {
        addStaticEffect(new StaticBoostEffect(-5, 0, GrantScope.ALL_CREATURES));
        target(TargetFilters.creature()).addTrigger(Triggers.chaosEnsues(), new MayEffect(
                new ConditionalEffect(
                        new TargetPermanentMatches(new PermanentPowerAtMostPredicate(0)),
                        new DestroyTargetPermanentEffect()),
                "Destroy target creature with power 0 or less?"));
    }
}
