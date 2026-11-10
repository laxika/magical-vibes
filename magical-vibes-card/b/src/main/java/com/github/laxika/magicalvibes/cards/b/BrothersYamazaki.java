package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.IgnoreLegendRuleWhenExactlyTwoSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "CHK", collectorNumber = "160a")
@CardRegistration(set = "CHK", collectorNumber = "160b")
public class BrothersYamazaki extends Card {

    public BrothersYamazaki() {
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, new BushidoEffect(1));
        addTrigger(Triggers.becomesBlocked(), new BushidoEffect(1));
        addStaticEffect(new IgnoreLegendRuleWhenExactlyTwoSameNameEffect());
        // "Each OTHER creature named Brothers Yamazaki" — any controller, so ALL_CREATURES
        // (which already excludes the source) narrowed by the name.
        addStaticEffect(new StaticBoostEffect(2, 2, Set.of(Keyword.HASTE),
                GrantScope.ALL_CREATURES, new PermanentNamedPredicate("Brothers Yamazaki")));
    }
}
