package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "171")
public class HazeOfPollen extends Card {

    public HazeOfPollen() {
        // Prevent all combat damage that would be dealt this turn.
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombat());

        // Cycling {3} ({3}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addHandActivatedAbility(new ActivatedAbility(false, "{3}",
                List.of(new DrawCardEffect(1)),
                "Cycling {3} ({3}, Discard this card: Draw a card.)"));
    }
}
