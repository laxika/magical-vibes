package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringSpellControllerConditionalEffect;

@CardRegistration(set = "SNC", collectorNumber = "17")
public class IlluminatorVirtuoso extends Card {

    public IlluminatorVirtuoso() {
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL,
                new TriggeringSpellControllerConditionalEffect(new DrawDiscardAndConniveEffect()));
    }
}
