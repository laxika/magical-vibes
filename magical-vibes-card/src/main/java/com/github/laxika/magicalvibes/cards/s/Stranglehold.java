package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.SkipExtraTurnReplacementEffect;

@CardRegistration(set = "SLD", collectorNumber = "1804")
public class Stranglehold extends Card {

    public Stranglehold() {
        addEffect(EffectSlot.STATIC, new OpponentsCantSearchLibrariesEffect());
        addEffect(EffectSlot.STATIC, new SkipExtraTurnReplacementEffect());
    }
}
