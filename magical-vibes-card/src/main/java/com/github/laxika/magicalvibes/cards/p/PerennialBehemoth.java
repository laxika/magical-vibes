package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromGraveyardEffect;

@CardRegistration(set = "BRO", collectorNumber = "202")
public class PerennialBehemoth extends Card {

    public PerennialBehemoth() {
        addEffect(EffectSlot.STATIC, new PlayLandsFromGraveyardEffect());
        addUnearth("{G}{G}");
    }
}
