package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllowCastTargetCardFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllowCastTargetCardFromGraveyardThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllowCastTargetCardFromGraveyardThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var permission = (AllowCastTargetCardFromGraveyardThisTurnEffect) effect;
        UUID controllerId = entry.getControllerId();

        List<UUID> targetCardIds = !entry.getTargetCardIds().isEmpty()
                ? entry.getTargetCardIds()
                : entry.getTargetId() == null ? List.of() : List.of(entry.getTargetId());

        for (UUID targetCardId : targetCardIds) {
            Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
            if (targetCard == null) {
                gameLogService.append(gameData, GameLog.text(entry.getDescription()
                        + " fizzles (target no longer in graveyard)."));
                continue;
            }

            UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());
            boolean validScope = graveyardOwnerId != null && switch (permission.scope()) {
                case OPPONENT_GRAVEYARD -> !graveyardOwnerId.equals(controllerId);
                case CONTROLLERS_GRAVEYARD -> graveyardOwnerId.equals(controllerId);
                case ALL_GRAVEYARDS -> true;
            };
            if (!validScope || !predicateEvaluationService.matchesCardPredicate(
                    targetCard, permission.filter(), null)) {
                gameLogService.append(gameData, GameLog.text(entry.getDescription()
                        + " fizzles (target is no longer legal)."));
                continue;
            }

            // The card stays in the graveyard; the permission and the exile replacement both hold
            // until turn cleanup drops them, so the controller may cast it whenever timing allows.
            gameData.graveyardPlayPermissions.put(targetCard.getId(), controllerId);
            gameData.graveyardPlayPermissionsExpireEndOfTurn.add(targetCard.getId());
            if (permission.entersTapped()) {
                gameData.graveyardCardsEnterTapped.add(targetCard.getId());
            }
            if (permission.exileInsteadOfGraveyard()) {
                gameData.exileInsteadOfGraveyard.add(targetCard.getId());
            }

            gameLogService.append(gameData, GameLog.builder()
                    .text(gameData.playerIdToName.get(controllerId) + " may cast ")
                    .card(targetCard)
                    .text(" from their graveyard this turn.")
                    .build());
            log.info("Game {} - {} may cast {} from their graveyard this turn",
                    gameData.id, gameData.playerIdToName.get(controllerId), targetCard.getName());
        }
    }
}
