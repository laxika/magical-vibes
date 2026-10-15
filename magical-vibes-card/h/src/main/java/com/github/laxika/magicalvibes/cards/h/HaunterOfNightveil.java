package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "DGM", collectorNumber = "76")
public class HaunterOfNightveil extends Card {

    public HaunterOfNightveil() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(-1, 0, GrantScope.OPPONENT_CREATURES));
    }
}
