package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LimDulsVaultSupport;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Lim-Dûl's Vault's "you may pay 1 life" prompt: after each look at the top five cards the
 * controller decides whether to bottom them and look at five more. Accepting pays 1 life (legal
 * only at a life total of at least 1, CR 119.4) and moves on to the bottom-ordering prompt;
 * declining moves on to the shuffle-and-put-on-top ordering prompt.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LimDulsVaultRepeatChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.LimDulsVaultRepeatChoice> {

    private final LifeSupport lifeSupport;
    private final GameLogService gameLogService;
    private final LimDulsVaultSupport limDulsVaultSupport;

    @Override
    public Class<PendingInteraction.LimDulsVaultRepeatChoice> handledType() {
        return PendingInteraction.LimDulsVaultRepeatChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.MayAbilityChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.LimDulsVaultRepeatChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your Lim-Dûl's Vault choice");
        }

        UUID controllerId = interaction.playerId();
        boolean accepted = ((InteractionAnswer.MayAbilityChosen) answer).accepted();
        if (accepted && gameData.getLife(controllerId) < 1) {
            throw new IllegalStateException("Cannot pay 1 life");
        }

        gameData.interaction.clearAwaitingInput();

        if (accepted) {
            lifeSupport.applyLifePayment(gameData, controllerId, 1, "Lim-Dûl's Vault");
            limDulsVaultSupport.beginOrder(gameData, controllerId, interaction.lookedAt(), true);
            return;
        }

        gameLogService.append(gameData, GameLog.text(
                player.getUsername() + " stops looking (Lim-Dûl's Vault)."));
        limDulsVaultSupport.beginOrder(gameData, controllerId, interaction.lookedAt(), false);
    }
}
