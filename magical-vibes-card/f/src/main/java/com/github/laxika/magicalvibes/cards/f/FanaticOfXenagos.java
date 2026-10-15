package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TributeEffect;
import com.github.laxika.magicalvibes.model.effect.TributeNotPaidEffect;

@CardRegistration(set = "BNG", collectorNumber = "147")
public class FanaticOfXenagos extends Card {

    public FanaticOfXenagos() {
        addEffect(EffectSlot.STATIC, new TributeEffect(1));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new TributeNotPaidEffect(new BoostSelfEffect(1, 1)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new TributeNotPaidEffect(new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF)));
    }
}
