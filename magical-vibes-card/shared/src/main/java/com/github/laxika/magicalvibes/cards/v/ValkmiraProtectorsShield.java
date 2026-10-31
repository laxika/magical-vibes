package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromOpponentSourcesEffect;

public class ValkmiraProtectorsShield extends Card {

    public ValkmiraProtectorsShield() {
        addStaticEffect(new PreventDamageFromOpponentSourcesEffect(1, true));
        addEffect(EffectSlot.ON_CONTROLLER_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
                new CounterUnlessPaysEffect(1));
        addEffect(EffectSlot.ON_ANOTHER_ALLY_PERMANENT_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
                new CounterUnlessPaysEffect(1));
    }
}
