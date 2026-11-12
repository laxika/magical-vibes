package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AttachTargetAuraOrEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AttachTargetAuraOrEquipmentToTargetCreatureHandler implements MayEffectHandlerBean {

    private final ETBTokenTargetService etbTokenTargetService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachTargetAuraOrEquipmentToTargetCreatureEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (!accepted) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        // The question is the resolving ability's own (CR 603.5), so the Aura or Equipment and the creature are
        // that entry's targets.
        if (gameData.pendingEffectResolutionEntry == null) {
            throw new IllegalStateException("\"You may attach\" is asked as its ability resolves: "
                    + ability.description());
        }
        gameData.queueInteractionFirst(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                ability.sourceCard(), ability.controllerId(), new ArrayList<>(ability.effects()),
                ability.sourcePermanentId(), List.of(), 0, 0, List.of(), 0, true));
        etbTokenTargetService.processNextETBTokenMultiTargetTrigger(gameData);

        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }
}
