package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExileAccessScope;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileBottomCardsToSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves source-linked exile from the bottom of a library. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileBottomCardsToSourceEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;
    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileBottomCardsToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileBottomCardsToSourceEffect exileEffect = (ExileBottomCardsToSourceEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent source = sourcePermanentId == null
                ? null
                : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        Card sourceCard = source != null ? source.getCard() : entry.getCard();
        List<AllowCastFromCardsExiledWithSourceEffect> persistentPermissions = sourceCard
                .getStaticEffects().stream()
                .filter(AllowCastFromCardsExiledWithSourceEffect.class::isInstance)
                .map(AllowCastFromCardsExiledWithSourceEffect.class::cast)
                .filter(AllowCastFromCardsExiledWithSourceEffect::persistsAfterSourceLeaves)
                .toList();
        int count = Math.max(0, amountEvaluationService.evaluate(gameData, exileEffect.count(),
                AmountContext.forStackEntry(entry, source)));
        if (count == 0) {
            return;
        }

        for (UUID playerId : subjectResolutionService.players(gameData, entry, exileEffect, exileEffect.player())) {
            exileBottomCards(gameData, exileEffect, playerId, sourceCard, sourcePermanentId, count,
                    persistentPermissions, controllerId);
        }
    }

    private void exileBottomCards(GameData gameData, ExileBottomCardsToSourceEffect effect,
                                  UUID playerId, Card sourceCard, UUID sourcePermanentId, int count,
                                  List<AllowCastFromCardsExiledWithSourceEffect> persistentPermissions,
                                  UUID sourceControllerId) {
        var library = gameData.playerDecks.get(playerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        int exiledCount = Math.min(count, library.size());
        for (int i = 0; i < exiledCount; i++) {
            Card card = library.removeLast();
            if (effect.faceDown()) {
                exileService.exileCardFaceDown(gameData, playerId, card, sourcePermanentId,
                        sourceControllerId);
            } else {
                exileService.exileCard(gameData, playerId, card, sourcePermanentId);
            }
            for (AllowCastFromCardsExiledWithSourceEffect permission : persistentPermissions) {
                UUID permittedPlayer = permission.accessScope() == ExileAccessScope.EXILER
                        ? playerId : sourceControllerId;
                if (effect.faceDown()) {
                    gameData.grantExileLookPermission(card.getId(), permittedPlayer);
                }
                if (permission.filter() == null || predicateEvaluationService.matchesCardPredicate(
                        card, permission.filter(), null)) {
                    gameData.grantExilePlayPermission(card.getId(), permittedPlayer, Duration.PERMANENT);
                    if (permission.anyManaType()) {
                        gameData.updateExilePlayPermission(card.getId(), granted -> granted.withAnyManaType(true));
                    }
                    break;
                }
            }
        }

        gameLogService.append(gameData, GameLog.builder()
                .text(gameData.playerIdToName.get(playerId) + " exiles " + exiledCount
                        + " card" + (exiledCount == 1 ? "" : "s") + " from the bottom of their library"
                        + (effect.faceDown() ? " face down" : "") + " with ")
                .card(sourceCard).text(".").build());
        log.info("Game {} - {} exiles {} cards from library bottom with {}", gameData.id,
                gameData.playerIdToName.get(playerId), exiledCount, sourceCard.getName());
    }
}
