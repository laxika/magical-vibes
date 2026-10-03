package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerAndMayCastSpellFromGraveyardEffect;

@CardRegistration(set = "LTC", collectorNumber = "504")
@CardRegistration(set = "LTC", collectorNumber = "548")
public class SorcerousSquall extends Card {

    public SorcerousSquall() {
        addEffect(EffectSlot.SPELL, new MillTargetPlayerAndMayCastSpellFromGraveyardEffect(9));
    }
}
