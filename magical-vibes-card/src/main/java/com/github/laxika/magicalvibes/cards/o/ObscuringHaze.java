package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerControlsCommander;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "308")
@CardRegistration(set = "CMM", collectorNumber = "566")
@CardRegistration(set = "CMM", collectorNumber = "701")
@CardRegistration(set = "TLE", collectorNumber = "313")
public class ObscuringHaze extends Card {

    public ObscuringHaze() {
        // If you control a commander, you may cast this spell without paying its mana cost.
        addCastingOption(new AlternateHandCast(List.of(), new ControllerControlsCommander(), false));

        // Prevent all damage that would be dealt this turn by creatures your opponents control.
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allByOpponentCreatures());
    }
}
