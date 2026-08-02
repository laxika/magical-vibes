package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesCanBlockAsThoughUntappedEffect;

@CardRegistration(set = "CHK", collectorNumber = "33")
public class MasakoTheHumorless extends Card {

    public MasakoTheHumorless() {
        addEffect(EffectSlot.STATIC, new ControlledCreaturesCanBlockAsThoughUntappedEffect());
    }
}
