package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "OTJ", collectorNumber = "120")
public class DemonicRuckus extends Card {

    public DemonicRuckus() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(
                        1, 1, Set.of(Keyword.MENACE, Keyword.TRAMPLE), GrantScope.ENCHANTED_CREATURE))
                .addTrigger(Triggers.dies(ObjectRelation.THIS), new DrawCardEffect(1));

        // Plot {R}
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{R}"))));
    }
}
