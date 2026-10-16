package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardWithDiscoveryCounterMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileTargetCardFromGraveyardWithDiscoveryCounterMayPlayThisTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCardFromGraveyardWithDiscoveryCounterMayPlayThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effectToResolve) {
        var effect = (ExileTargetCardFromGraveyardWithDiscoveryCounterMayPlayThisTurnEffect) effectToResolve;
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null && entry.getTargetCardIds() != null
                && !entry.getTargetCardIds().isEmpty()) {
            targetCardId = entry.getTargetCardIds().getFirst();
        }
        if (targetCardId == null) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription() + " — no target selected."));
        } else {
            exileTarget(gameData, entry, effect, targetCardId);
        }
        grantPlayPermissions(gameData, entry);
    }

    private void exileTarget(GameData gameData, StackEntry entry,
                             ExileTargetCardFromGraveyardWithDiscoveryCounterMayPlayThisTurnEffect effect,
                             UUID targetCardId) {
        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target is no longer in a graveyard)."));
            return;
        }

        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCardId);
        if (graveyardOwnerId == null || (effect.filter() != null
                && !predicateEvaluationService.matchesCardPredicate(
                targetCard, effect.filter(), entry.getCard().getId(), gameData, graveyardOwnerId))) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target no longer matches its restriction)."));
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, targetCardId);
        gameData.addToExileWithDiscoveryCounter(graveyardOwnerId, targetCard);

        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(entry.getControllerId()) + " exiles ", targetCard,
                " from a graveyard with a discovery counter."));
    }

    /**
     * "You may play a card from exile with a discovery counter on it this turn": every card in exile with a
     * discovery counter qualifies, but only one of them may be played.
     */
    private void grantPlayPermissions(GameData gameData, StackEntry entry) {
        List<UUID> playable = gameData.exiledCards.stream()
                .map(exiled -> exiled.card().getId())
                .filter(gameData.exiledCardsWithDiscoveryCounters::contains)
                .toList();
        if (playable.isEmpty()) {
            return;
        }
        for (UUID cardId : playable) {
            gameData.exilePlayPermissions.put(cardId, entry.getControllerId());
            gameData.exilePlayPermissionsExpireEndOfTurn.add(cardId);
        }
        gameData.registerExilePlayPermissionGroup(UUID.randomUUID(), 1, playable);
    }
}
