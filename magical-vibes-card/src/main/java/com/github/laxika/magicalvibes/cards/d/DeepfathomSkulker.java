package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "43")
public class DeepfathomSkulker extends Card {

    public DeepfathomSkulker() {
        // Whenever a creature you control deals combat damage to a player, you may draw a card.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        null,
                        new MayEffect(new DrawCardEffect(1), "Draw a card?")));

        // {3}{C}: Target creature can't be blocked this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{C}",
                List.of(new MakeCreatureUnblockableEffect()),
                "{3}{C}: Target creature can't be blocked this turn.",
                TargetFilters.creature()));
    }
}
