package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExploitEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "VOW", collectorNumber = "81")
public class StitchedAssistant extends Card {

    public StitchedAssistant() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new ExploitEffect(), "Sacrifice a creature?"));

        addEffect(EffectSlot.ON_EXPLOIT, new ScryEffect(1));
        addEffect(EffectSlot.ON_EXPLOIT, new DrawCardEffect(1));
    }
}
