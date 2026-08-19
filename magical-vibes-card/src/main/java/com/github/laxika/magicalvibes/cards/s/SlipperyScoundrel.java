package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "RIX", collectorNumber = "55")
public class SlipperyScoundrel extends Card {

    public SlipperyScoundrel() {
        addEffect(EffectSlot.STATIC, new AscendEffect());
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerHasCityBlessing(),
                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerHasCityBlessing(),
                new GrantEffectEffect(new CantBeBlockedEffect(), GrantScope.SELF)));
    }
}
