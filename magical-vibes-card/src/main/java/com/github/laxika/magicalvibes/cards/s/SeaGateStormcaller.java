package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

@CardRegistration(set = "ZNR", collectorNumber = "77")
public class SeaGateStormcaller extends Card {

    public SeaGateStormcaller() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{4}{U}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CopyNextInstantOrSorceryCastThisTurnEffect(2));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new Kicked(), new CopyNextInstantOrSorceryCastThisTurnEffect(2)));
    }
}
