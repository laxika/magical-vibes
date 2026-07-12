package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "9ED", collectorNumber = "105")
@CardRegistration(set = "8ED", collectorNumber = "108")
public class TidalKraken extends Card {

    public TidalKraken() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
    }
}
