package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentOrLoseLifeEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SacrificePermanentOrLoseLifeEffect}: the player on the stack entry's
 * {@code targetId} (the enchanted player, for a Curse upkeep trigger) sacrifices one permanent
 * matching the filter of their choice; if they control none, they lose the fallback life instead.
 *
 * <p>The single-select choice reuses {@code PermanentChoiceContext.SacrificeCreature}, which
 * sacrifices whichever permanent id the player picks for that player — the eligible ids handed to
 * the prompt already enforce the filter. Cruel Reality is the canonical user.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificePermanentOrLoseLifeEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;
    private final LifeSupport lifeSupport;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificePermanentOrLoseLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificePermanentOrLoseLifeEffect) effect;

        UUID playerId = entry.getTargetId();
        if (playerId == null || !gameData.playerIds.contains(playerId)) {
            return;
        }

        List<UUID> matchingIds = destructionSupport.collectPermanentIds(gameData, playerId,
                p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, e.filter()));

        if (matchingIds.isEmpty()) {
            lifeSupport.applyLifeLoss(gameData, playerId, e.lifeLoss(), entry.getCard().getName());
            gameOutcomeService.checkWinCondition(gameData);
            return;
        }

        if (matchingIds.size() == 1) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, matchingIds.getFirst());
            if (permanent != null) {
                destructionSupport.sacrificeAndLog(gameData, permanent, playerId);
            }
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.SacrificeCreature(playerId));
        playerInputService.beginPermanentChoice(gameData, playerId, matchingIds,
                "Choose a permanent to sacrifice.");
        String playerName = gameData.playerIdToName.get(playerId);
        gameBroadcastService.logAndBroadcast(gameData,
                GameLog.text(playerName + " must sacrifice a permanent to " + entry.getCard().getName() + "."));
    }
}
