package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControllerLifeAtOrBelowThresholdConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "DKA", collectorNumber = "130")
public class VillageSurvivors extends Card {

    public VillageSurvivors() {
        // Fateful hour — As long as you have 5 or less life, other creatures you control have vigilance.
        addEffect(EffectSlot.STATIC, new ControllerLifeAtOrBelowThresholdConditionalEffect(5,
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.OWN_CREATURES)));
    }
}
