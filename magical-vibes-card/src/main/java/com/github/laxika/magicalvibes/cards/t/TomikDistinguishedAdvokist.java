package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantPlayLandsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantTargetLandsEffect;

@CardRegistration(set = "WAR", collectorNumber = "34")
public class TomikDistinguishedAdvokist extends Card {

    public TomikDistinguishedAdvokist() {
        addEffect(EffectSlot.STATIC, new OpponentsCantTargetLandsEffect());
        addEffect(EffectSlot.STATIC, new OpponentsCantPlayLandsFromGraveyardEffect());
    }
}
