package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "STH", collectorNumber = "33")
public class Hesitation extends Card {

    public Hesitation() {
        // When a player casts a spell, sacrifice this enchantment and counter that spell.
        addTrigger(Triggers.playerCastsSpell(), new SpellCastTriggerEffect(
                null,
                List.of(new SacrificeSelfEffect(), new CounterSpellEffect())
        ));
    }
}
