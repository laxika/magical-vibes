package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.SkipDrawStepEffect;

@CardRegistration(set = "JUD", collectorNumber = "24")
public class SolitaryConfinement extends Card {

    public SolitaryConfinement() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SacrificeUnlessDiscardCardTypeEffect(null));
        addEffect(EffectSlot.STATIC, new SkipDrawStepEffect());
        addEffect(EffectSlot.STATIC, new GrantControllerKeywordEffect(Keyword.SHROUD));
        addEffect(EffectSlot.STATIC, new PreventAllDamageToControllerEffect());
    }
}
