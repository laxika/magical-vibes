package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TributeEffect;
import com.github.laxika.magicalvibes.model.effect.TributeNotPaidEffect;

@CardRegistration(set = "BNG", collectorNumber = "113")
public class ThunderBrute extends Card {

    public ThunderBrute() {
        addEffect(EffectSlot.STATIC, new TributeEffect(3));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TributeNotPaidEffect(
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF)));
    }
}
