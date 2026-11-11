package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AttachTargetEquipmentToTargetCreatureHandler implements MayEffectHandlerBean {

    private final ETBTokenTargetService etbTokenTargetService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachTargetEquipmentToTargetCreatureEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (!accepted) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // With an entry resolving, the targets are that entry's. Otherwise the accepted ability goes on the stack with
        // them, and that entry records what the question kept of the trigger event.
        boolean targetsOfResolvingEntry = gameData.pendingEffectResolutionEntry != null;
        Runnable targetWalk = () -> {
            gameData.queueInteractionFirst(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                    ability.sourceCard(), ability.controllerId(), new ArrayList<>(ability.effects()),
                    ability.sourcePermanentId(), List.of(), 0, 0, List.of(), 0, targetsOfResolvingEntry));
            etbTokenTargetService.processNextETBTokenMultiTargetTrigger(gameData);
        };
        if (targetsOfResolvingEntry) {
            targetWalk.run();
        } else {
            ReflexiveAbilitySupport.route(gameData, ReflexiveAbilitySupport.mayAbilityRecord(ability,
                    ability.effects(), ability.sourceCard().getName() + "'s ability", ability.sourcePermanentId(),
                    0), targetWalk);
        }

        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }
}
