package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "WOE", collectorNumber = "41")
public class ArchiveDragon extends Card {

    public ArchiveDragon() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(2));
    }
}
