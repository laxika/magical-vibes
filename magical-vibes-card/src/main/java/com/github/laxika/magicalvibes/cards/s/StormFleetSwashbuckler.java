package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "RIX", collectorNumber = "117")
public class StormFleetSwashbuckler extends Card {

    public StormFleetSwashbuckler() {
        addEffect(EffectSlot.STATIC, new AscendEffect());
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerHasCityBlessing(), new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.SELF)));
    }
}
