package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromTargetCreatureToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FIC", collectorNumber = "5")
@CardRegistration(set = "FIC", collectorNumber = "187")
@CardRegistration(set = "FIC", collectorNumber = "205")
@CardRegistration(set = "FIC", collectorNumber = "213")
@CardRegistration(set = "FIC", collectorNumber = "224")
public class TidusYunasGuardian extends Card {

    public TidusYunasGuardian() {
        target(TargetFilters.creatureYouControl(), 0, 2)
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new MayEffect(
                                new MoveCounterFromTargetCreatureToTargetCreatureEffect(),
                                "Move a counter?"));

        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new OncePerTurnTriggerEffect(new AllyCombatDamageTriggerEffect(
                        new PermanentHasCountersPredicate(CounterType.ANY),
                        new MayEffect(
                                SequenceEffect.of(new DrawCardEffect(), new ProliferateEffect()),
                                "Draw a card and proliferate?"),
                        false,
                        true)));
    }
}
