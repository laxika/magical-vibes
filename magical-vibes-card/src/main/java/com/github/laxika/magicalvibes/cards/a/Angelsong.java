package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageEffect;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "4")
public class Angelsong extends Card {

    public Angelsong() {
        // Prevent all combat damage that would be dealt this turn.
        addEffect(EffectSlot.SPELL, new PreventAllCombatDamageEffect());

        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addHandActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new DrawCardEffect(1)),
                "Cycling {2} ({2}, Discard this card: Draw a card.)"));
    }
}
