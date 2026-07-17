package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "7ED", collectorNumber = "44")
@CardRegistration(set = "5ED", collectorNumber = "63")
public class ShieldWall extends Card {

    public ShieldWall() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(0, 2));
    }
}
