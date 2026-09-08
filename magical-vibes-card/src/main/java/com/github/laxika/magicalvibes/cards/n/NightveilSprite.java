package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "GRN", collectorNumber = "48")
public class NightveilSprite extends Card {

    public NightveilSprite() {
        addEffect(EffectSlot.ON_ATTACK, new SurveilEffect(1));
    }
}
