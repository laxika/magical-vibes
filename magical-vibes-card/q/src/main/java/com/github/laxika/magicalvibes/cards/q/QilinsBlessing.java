package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "GS1", collectorNumber = "14")
public class QilinsBlessing extends Card {

    public QilinsBlessing() {
        // Target creature gets +2/+2 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 2));
    }
}
