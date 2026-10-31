package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
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
        addStaticEffect(new AscendEffect());
        addStaticEffect(new ConditionalEffect(new ControllerHasCityBlessing(),
                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF)));
        addStaticEffect(new ConditionalEffect(new ControllerHasCityBlessing(),
                new GrantEffectEffect(new CantBeBlockedEffect(), GrantScope.SELF)));
    }
}
