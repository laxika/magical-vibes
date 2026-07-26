package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.combat.CombatResult;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles the active player's attacker declaration answer. The canonical event subscriber owns
 * prompt projection from the finalized metadata on the pending interaction.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttackerDeclarationInteractionHandler
        implements InteractionHandler<PendingInteraction.AttackerDeclaration> {

    private final CombatService combatService;
    private final StateBasedActionService stateBasedActionService;
    private final TurnProgressionService turnProgressionService;

    @Override
    public Class<PendingInteraction.AttackerDeclaration> handledType() {
        return PendingInteraction.AttackerDeclaration.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.AttackersDeclared.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.AttackerDeclaration interaction,
                             InteractionAnswer answer) {
        InteractionAnswer.AttackersDeclared declared = (InteractionAnswer.AttackersDeclared) answer;
        CombatResult result = combatService.declareAttackers(
                gameData, player, declared.attackerIndices(), declared.attackTargets(), declared.bands());
        // Declaring attackers can pay life (Norn's Annex) or sacrifice permanents.
        // Check SBAs before anyone receives priority or combat advances.
        stateBasedActionService.performStateBasedActions(gameData);
        if (gameData.status == GameStatus.RUNNING) {
            turnProgressionService.handleCombatResult(result, gameData);
        }
    }
}
