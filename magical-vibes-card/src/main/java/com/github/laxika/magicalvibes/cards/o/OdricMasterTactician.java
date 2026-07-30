package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.ChooseBlockersThisCombatEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

/**
 * Odric, Master Tactician — First strike. Whenever Odric and at least three other creatures attack,
 * you choose which creatures block this combat and how those creatures block.
 */
@CardRegistration(set = "M13", collectorNumber = "23")
public class OdricMasterTactician extends Card {

    public OdricMasterTactician() {
        // First strike is loaded from Scryfall keywords.
        // Odric + 3 others = 4 attackers total; ON_ATTACK ensures Odric himself is attacking.
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new MinimumAttackers(4),
                new ChooseBlockersThisCombatEffect()));
    }
}
