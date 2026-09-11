package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OHOP", collectorNumber = "16")
public class TheHippodrome extends Card {

    public TheHippodrome() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(-5, 0, GrantScope.ALL_CREATURES));
        target(TargetFilters.creature()).addEffect(EffectSlot.CHAOS_TRIGGERED, new MayEffect(
                new ConditionalEffect(
                        new TargetPermanentMatches(new PermanentPowerAtMostPredicate(0)),
                        new DestroyTargetPermanentEffect()),
                "Destroy target creature with power 0 or less?"));
    }
}
