package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "ONS", collectorNumber = "232")
@CardRegistration(set = "DMR", collectorNumber = "138")
@CardRegistration(set = "C13", collectorNumber = "119")
@CardRegistration(set = "CMM", collectorNumber = "255")
@CardRegistration(set = "C20", collectorNumber = "159")
public class SliceAndDice extends Card {

    public SliceAndDice() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(4));

        addCycling("{2}{R}");
        // "When you cycle this card" is its own triggered ability, so it goes on the stack above the
        // cycling draw and resolves first.
        addEffect(EffectSlot.ON_SELF_CYCLED, new MayEffect(
                new MassDamageEffect(1),
                "Have Slice and Dice deal 1 damage to each creature?"));
    }
}
