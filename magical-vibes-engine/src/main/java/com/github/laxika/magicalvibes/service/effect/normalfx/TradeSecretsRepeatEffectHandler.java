package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TradeSecretsRepeatEffect;
import com.github.laxika.magicalvibes.service.effect.mayfx.MayEffectHandlerBean;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves Trade Secrets' opponent-controlled repeat choice. The spell's effect list remains
 * parked while the opponent decides; accepting restarts it at the first draw effect.
 */
@Component
@RequiredArgsConstructor
public class TradeSecretsRepeatEffectHandler
        implements NormalEffectHandlerBean, MayEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TradeSecretsRepeatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID opponentId = entry.getTargetId();
        if (opponentId == null || !gameData.playerIds.contains(opponentId)) {
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        gameData.queueMayAbilityForPlayer(
                entry.getCard(),
                entry.getControllerId(),
                new MayEffect(new TradeSecretsRepeatEffect(), "Repeat this process?"),
                null,
                entry.getSourcePermanentId(),
                opponentId,
                entry.getSourcePermanentSnapshot());
        playerInputService.processNextMayAbility(gameData);
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        UUID choicePlayerId = ability.choicePlayerId() != null
                ? ability.choicePlayerId()
                : ability.controllerId();
        if (!player.getId().equals(choicePlayerId)) {
            throw new IllegalStateException("Not your Trade Secrets choice");
        }

        StackEntry entry = gameData.pendingEffectResolutionEntry;
        gameData.rerunCurrentEffectAfterInteraction = false;
        if (entry != null) {
            gameData.pendingEffectResolutionIndex = accepted
                    ? 0
                    : entry.getEffectsToResolve().size();
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
