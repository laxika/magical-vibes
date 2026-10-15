package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

@CardRegistration(set = "DMU", collectorNumber = "20")
public class HeroicCharge extends Card {

    public HeroicCharge() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{1}{R}"));
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(2, 1));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new Kicked(),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES)));
    }
}
