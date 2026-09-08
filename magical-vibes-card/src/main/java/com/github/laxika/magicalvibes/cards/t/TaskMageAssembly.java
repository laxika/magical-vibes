package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "105")
public class TaskMageAssembly extends Card {

    public TaskMageAssembly() {
        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) -> gameData.orderedPlayerIds.stream()
                        .flatMap(playerId -> gameData.playerBattlefields
                                .getOrDefault(playerId, List.of()).stream())
                        .noneMatch(permanent -> permanent.getCard().hasType(CardType.CREATURE)),
                List.of(new SacrificeSelfEffect()),
                "Task Mage Assembly's state-triggered ability"
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new DealDamageToTargetCreatureEffect(1)),
                "{2}: This enchantment deals 1 damage to target creature. Any player may activate this ability but only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivatableByAnyPlayer());
    }
}
