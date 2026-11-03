package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OGW", collectorNumber = "155")
@CardRegistration(set = "DDS", collectorNumber = "23")
public class JoriEnRuinDiver extends Card {

    public JoriEnRuinDiver() {
        addTrigger(Triggers.youCastSpell().nthThisTurn(2), new DrawCardEffect());
    }
}
