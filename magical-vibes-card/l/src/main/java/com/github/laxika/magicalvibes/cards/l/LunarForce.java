package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "EMN", collectorNumber = "68")
public class LunarForce extends Card {

    public LunarForce() {
        addTrigger(Triggers.opponentCastsSpell(), new SpellCastTriggerEffect(
                null,
                List.of(new SacrificeSelfEffect(), new CounterSpellEffect())
        ));
    }
}
