package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.input.MayAbilityHandlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Handles may-ability accept/decline interactions. The prompt mirrors the head of
 * {@link GameData#pendingMayAbilities}; the answer is applied by
 * {@link MayAbilityHandlerService}, which owns the per-effect dispatch (cast-from-zone,
 * penalty choices, copy effects, targeted triggers, …) and removes the queue head.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MayAbilityChoiceInteractionHandler implements InteractionHandler<PendingInteraction.MayAbilityChoice> {

    private final MayAbilityHandlerService mayAbilityHandlerService;

    @Override
    public Class<PendingInteraction.MayAbilityChoice> handledType() {
        return PendingInteraction.MayAbilityChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.MayAbilityChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.MayAbilityChoice interaction,
                             InteractionAnswer answer) {
        boolean accepted = ((InteractionAnswer.MayAbilityChosen) answer).accepted();
        mayAbilityHandlerService.handleMayAbilityChosen(gameData, player, accepted);
    }
}
