package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOneOfCombatDamageDealersThenRevealMatchingCreatureEffect;

@CardRegistration(set = "ECC", collectorNumber = "93")
public class DescendantsFury extends Card {

    public DescendantsFury() {
        // Whenever one or more creatures you control deal combat damage to a player, you may
        // sacrifice one of them. If you do, reveal until a creature sharing its type enters.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        null,
                        new MayEffect(
                                new SacrificeOneOfCombatDamageDealersThenRevealMatchingCreatureEffect(),
                                "Sacrifice one of those creatures?"),
                        false,
                        true));
    }
}
