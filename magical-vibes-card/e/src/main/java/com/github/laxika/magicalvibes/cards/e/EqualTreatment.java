package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceDamageAboveThresholdEffect;

@CardRegistration(set = "TOR", collectorNumber = "4")
public class EqualTreatment extends Card {

    public EqualTreatment() {
        addSpellEffect(GrantStaticEffectToPlayerEffect.untilEndOfTurn(new ReplaceDamageAboveThresholdEffect(1, 2)));
        addSpellEffect(new DrawCardEffect());
    }
}
