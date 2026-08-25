package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SelfExiledWhileActivatingCraftAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "LCI", collectorNumber = "22")
public class MarketGnome extends Card {

    public MarketGnome() {
        SequenceEffect gainLifeAndDraw = SequenceEffect.of(new GainLifeEffect(1), new DrawCardEffect());
        addEffect(EffectSlot.ON_DEATH, gainLifeAndDraw);
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new SelfExiledWhileActivatingCraftAbilityEffect(gainLifeAndDraw));
    }
}
