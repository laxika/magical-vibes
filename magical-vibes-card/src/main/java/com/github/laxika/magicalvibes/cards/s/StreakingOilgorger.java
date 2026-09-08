package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "DFT", collectorNumber = "107")
public class StreakingOilgorger extends Card {

    public StreakingOilgorger() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new MaxSpeed(),
                new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF)));
    }
}
