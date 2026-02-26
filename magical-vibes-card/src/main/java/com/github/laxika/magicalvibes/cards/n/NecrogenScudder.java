package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

@CardRegistration(set = "SOM", collectorNumber = "71")
public class NecrogenScudder extends Card {

    public NecrogenScudder() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LoseLifeEffect(3));
    }
}
