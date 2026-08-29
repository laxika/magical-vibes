package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeEquippedEffect;

@CardRegistration(set = "5DN", collectorNumber = "66")
public class GoblinBrawler extends Card {

    public GoblinBrawler() {
        addEffect(EffectSlot.STATIC, new CantBeEquippedEffect());
    }
}
