package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "AER", collectorNumber = "53")
public class BattleAtTheBridge extends Card {

    public BattleAtTheBridge() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(
                new Scaled(new XValue(), -1),
                new Scaled(new XValue(), -1)
        ));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new XValue()));
    }
}
