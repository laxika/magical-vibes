package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesPermanentExileEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a targeted player's choice of a matching permanent to exile. */
@Component
@RequiredArgsConstructor
@Slf4j
public class TargetPlayerChoosesPermanentExileEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final ExileSupport exileSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerChoosesPermanentExileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        TargetPlayerChoosesPermanentExileEffect exileEffect =
                (TargetPlayerChoosesPermanentExileEffect) effect;
        List<UUID> matchingIds = destructionSupport.collectPermanentIds(gameData, targetPlayerId,
                permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, exileEffect.filter()));

        if (matchingIds.isEmpty()) {
            String playerName = gameData.playerIdToName.get(targetPlayerId);
            gameLogService.append(gameData, GameLog.text(playerName + " has no "
                    + exileEffect.permanentLabel() + "s to exile."));
            log.info("Game {} - {} has no {} to exile", gameData.id, playerName,
                    exileEffect.permanentLabel());
            return;
        }

        if (matchingIds.size() == 1) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, matchingIds.getFirst());
            if (permanent != null) {
                exilePermanent(gameData, permanent, entry.getCard().getName());
            }
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ExileChosenPermanent(
                targetPlayerId, entry.getCard().getName(), exileEffect.permanentLabel()));
        playerInputService.beginPermanentChoice(gameData, targetPlayerId, matchingIds,
                "Choose an " + exileEffect.permanentLabel() + " to exile.");
    }

    private void exilePermanent(GameData gameData, Permanent permanent, String sourceCardName) {
        exileSupport.exilePermanentAndLog(gameData, permanent, sourceCardName);
    }
}
