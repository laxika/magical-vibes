package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "42")
public class BidentOfThassa extends Card {

    public BidentOfThassa() {
        var opponentCreatures = new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate());

        // Whenever a creature you control deals combat damage to a player, you may draw a card.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        null,
                        new MayEffect(new DrawCardEffect(1), "Draw a card?")));

        // {1}{U}, {T}: Creatures your opponents control attack this turn if able.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{U}",
                List.of(new MatchingCreaturesMustAttackThisTurnEffect(opponentCreatures)),
                "{1}{U}, {T}: Creatures your opponents control attack this turn if able."
        ));
    }
}
