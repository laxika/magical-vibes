package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "5DN", collectorNumber = "52")
public class LoseHope extends Card {

    public LoseHope() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-1, -1));
        addEffect(EffectSlot.SPELL, new ScryEffect(2));
    }
}
