package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "M21", collectorNumber = "85")
public class WallOfRunes extends Card {

    public WallOfRunes() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(1));
    }
}
