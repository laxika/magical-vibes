package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OpponentExtraTurnSkipReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantSearchLibrariesAtAllEffect;

@CardRegistration(set = "CMD", collectorNumber = "136")
public class Stranglehold extends Card {

    public Stranglehold() {
        addEffect(EffectSlot.STATIC, new OpponentsCantSearchLibrariesAtAllEffect());
        addEffect(EffectSlot.STATIC, new OpponentExtraTurnSkipReplacementEffect());
    }
}
