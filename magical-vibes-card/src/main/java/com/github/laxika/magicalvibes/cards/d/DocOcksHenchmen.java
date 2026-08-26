package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;

@CardRegistration(set = "SPM", collectorNumber = "30")
public class DocOcksHenchmen extends Card {

    public DocOcksHenchmen() {
        addEffect(EffectSlot.ON_ATTACK, new DrawDiscardAndConniveEffect());
    }
}
