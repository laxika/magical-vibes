package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.IgnoreLegendRuleWhenExactlyTwoSameNameEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.Set;

@CardRegistration(set = "CHK", collectorNumber = "160a")
@CardRegistration(set = "CHK", collectorNumber = "160b")
public class BrothersYamazaki extends Card {

    public BrothersYamazaki() {
        addEffect(EffectSlot.ON_BLOCK, new BushidoEffect(1));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BushidoEffect(1));
        addEffect(EffectSlot.STATIC, new IgnoreLegendRuleWhenExactlyTwoSameNameEffect());
        // "Each OTHER creature named Brothers Yamazaki" — any controller, so ALL_CREATURES
        // (which already excludes the source) narrowed by the name.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, Set.of(Keyword.HASTE),
                GrantScope.ALL_CREATURES, new PermanentNamedPredicate("Brothers Yamazaki")));
    }
}
