package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "197")
public class JeskaiShrinekeeper extends Card {

    public JeskaiShrinekeeper() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                SequenceEffect.of(new GainLifeEffect(1), new DrawCardEffect(1)));
    }
}
