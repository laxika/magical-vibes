package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GoadEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnSourceAuraToCreatureOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "1843")
@CardRegistration(set = "CMM", collectorNumber = "732")
@CardRegistration(set = "CMM", collectorNumber = "763")
@CardRegistration(set = "SOC", collectorNumber = "214")
public class GhoulishImpetus extends Card {

    public GhoulishImpetus() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GoadEquippedCreatureEffect())
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        new RegisterDelayedReturnSourceAuraToCreatureOnDeathEffect());
    }
}
