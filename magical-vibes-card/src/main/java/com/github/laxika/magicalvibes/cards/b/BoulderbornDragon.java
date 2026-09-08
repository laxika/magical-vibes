package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "TDM", collectorNumber = "239")
public class BoulderbornDragon extends Card {

    public BoulderbornDragon() {
        addEffect(EffectSlot.ON_ATTACK, new SurveilEffect(1));
    }
}
