package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlledCommanderAsCast;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "TLE", collectorNumber = "313")
public class ObscuringHaze extends Card {

    public ObscuringHaze() {
        addCastingOption(new AlternateHandCast(List.of(), new ControlledCommanderAsCast(), false));
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allByOpponentCreatures());
    }
}
