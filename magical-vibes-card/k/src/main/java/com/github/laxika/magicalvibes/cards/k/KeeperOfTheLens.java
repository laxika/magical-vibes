package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtFaceDownCreaturesEffect;

@CardRegistration(set = "DTK", collectorNumber = "240")
public class KeeperOfTheLens extends Card {

    public KeeperOfTheLens() {
        addEffect(EffectSlot.STATIC, new LookAtFaceDownCreaturesEffect());
    }
}
