package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles the defending player's blocker declaration answer. The canonical event subscriber
 * owns prompt projection from finalized legal-pair and requirement metadata.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BlockerDeclarationInteractionHandler
        implements InteractionHandler<PendingInteraction.BlockerDeclaration> {

    private final CombatService combatService;
    private final TurnProgressionService turnProgressionService;

    @Override
    public Class<PendingInteraction.BlockerDeclaration> handledType() {
        return PendingInteraction.BlockerDeclaration.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.BlockersDeclared.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.BlockerDeclaration interaction,
                             InteractionAnswer answer) {
        InteractionAnswer.BlockersDeclared declared = (InteractionAnswer.BlockersDeclared) answer;
        turnProgressionService.handleCombatResult(
                combatService.declareBlockers(
                        gameData, player, declared.blockerAssignments()),
                gameData);
    }
}
