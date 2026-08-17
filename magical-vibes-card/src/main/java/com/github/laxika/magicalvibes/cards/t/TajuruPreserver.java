package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OpponentEffectsCantCauseSacrificeEffect;

@CardRegistration(set = "ROE", collectorNumber = "211")
public class TajuruPreserver extends Card {

    public TajuruPreserver() {
        addEffect(EffectSlot.STATIC, new OpponentEffectsCantCauseSacrificeEffect());
    }
}
