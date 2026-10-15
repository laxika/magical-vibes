package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.WayBehind;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "282")
@CardRegistration(set = "MB2", collectorNumber = "518")
public class KnightOfLostCauses extends Card {

    public KnightOfLostCauses() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new WayBehind(),
                new StaticBoostEffect(3, 3, Set.of(Keyword.INDESTRUCTIBLE), GrantScope.SELF)));
    }
}
