package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromHandAndBecomeForetoldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "KHC", collectorNumber = "16")
public class EtherealValkyrie extends Card {

    public EtherealValkyrie() {
        SequenceEffect drawThenForetell = SequenceEffect.of(
                new DrawCardEffect(), new ExileCardFromHandAndBecomeForetoldEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, drawThenForetell);
        addEffect(EffectSlot.ON_ATTACK, drawThenForetell);
    }
}
