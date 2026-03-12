package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M11", collectorNumber = "66")
public class MerfolkSpy extends Card {

    public MerfolkSpy() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new RevealRandomCardFromTargetPlayerHandEffect());
    }
}
