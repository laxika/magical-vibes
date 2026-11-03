package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EMN", collectorNumber = "68")
public class LunarForce extends Card {

    public LunarForce() {
        addTrigger(Triggers.opponentCastsSpell().anySpell(), new SacrificeSelfEffect(), new CounterSpellEffect());
    }
}
