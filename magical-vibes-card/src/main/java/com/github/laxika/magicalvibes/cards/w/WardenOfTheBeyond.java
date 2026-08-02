package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentOwnsCardInExile;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "M15", collectorNumber = "42")
public class WardenOfTheBeyond extends Card {

    public WardenOfTheBeyond() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new OpponentOwnsCardInExile(),
                new StaticBoostEffect(2, 2, GrantScope.SELF)));
    }
}
