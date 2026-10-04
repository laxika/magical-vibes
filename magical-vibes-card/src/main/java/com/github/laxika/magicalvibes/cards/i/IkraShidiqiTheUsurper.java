package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourceToughness;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "TDC", collectorNumber = "100")
@CardRegistration(set = "C16", collectorNumber = "32")
@CardRegistration(set = "CM2", collectorNumber = "11")
public class IkraShidiqiTheUsurper extends Card {

    public IkraShidiqiTheUsurper() {
        // Whenever a creature you control deals combat damage to a player, you gain life equal to
        // that creature's toughness.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        null, new GainLifeEffect(new SourceToughness()), true));
    }
}
