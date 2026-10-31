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
@CardRegistration(set = "MH2", collectorNumber = "265")
public class SolitaryConfinement extends Card {

    public SolitaryConfinement() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SacrificeUnlessDiscardCardTypeEffect(null));
        addStaticEffect(new SkipDrawStepEffect());
        addStaticEffect(new GrantControllerKeywordEffect(Keyword.SHROUD));
        addStaticEffect(new PreventAllDamageToControllerEffect());
    }
}
