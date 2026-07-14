package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesLifePerCreatureControlledEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageEffect;

@CardRegistration(set = "EVE", collectorNumber = "81")
public class BatwingBrume extends Card {

    public BatwingBrume() {
        // Prevent all combat damage that would be dealt this turn if {W} was spent to cast this spell.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ColorSpentToCast(ManaColor.WHITE),
                new PreventAllCombatDamageEffect()));

        // Each player loses 1 life for each attacking creature they control if {B} was spent to cast this spell.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ColorSpentToCast(ManaColor.BLACK),
                new EachPlayerLosesLifePerCreatureControlledEffect(1, true)));
    }
}
