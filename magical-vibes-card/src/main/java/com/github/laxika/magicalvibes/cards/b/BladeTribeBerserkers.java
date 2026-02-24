package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;

@CardRegistration(set = "SOM", collectorNumber = "84")
public class BladeTribeBerserkers extends Card {

    public BladeTribeBerserkers() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MetalcraftConditionalEffect(new BoostSelfEffect(3, 3)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MetalcraftConditionalEffect(new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF)));
    }
}
