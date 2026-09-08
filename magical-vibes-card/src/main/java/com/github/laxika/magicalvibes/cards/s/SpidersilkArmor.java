package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "MMQ", collectorNumber = "273")
public class SpidersilkArmor extends Card {

    public SpidersilkArmor() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 1, Set.of(Keyword.REACH), GrantScope.OWN_CREATURES));
    }
}
