package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTargetGraveyardCardCastEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantTargetGraveyardCardCastEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantTargetGraveyardCardCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GrantTargetGraveyardCardCastEffect e = (GrantTargetGraveyardCardCastEffect) effect;

        UUID targetCardId = !entry.getTargetCardIds().isEmpty()
                ? entry.getTargetCardIds().getFirst()
                : entry.getTargetId();
        if (targetCardId == null) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription() + " — no target selected."));
            return;
        }

        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription()
                    + " fizzles (target no longer in graveyard)."));
            return;
        }

        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());
        boolean validScope = graveyardOwnerId != null && switch (e.scope()) {
            case OPPONENT_GRAVEYARD -> !graveyardOwnerId.equals(entry.getControllerId());
            case CONTROLLERS_GRAVEYARD -> graveyardOwnerId.equals(entry.getControllerId());
            case ALL_GRAVEYARDS -> true;
        };
        if (!validScope) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription()
                    + " fizzles (target not in a valid graveyard)."));
            return;
        }

        if (e.filter() != null
                && !predicateEvaluationService.matchesCardPredicate(targetCard, e.filter(), entry.getCard().getId())) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription()
                    + " fizzles (target no longer matches)."));
            return;
        }

        gameData.graveyardCardCastPermissionsUntilEndOfTurn.put(targetCard.getId(),
                new GameData.GraveyardCardCastPermission(entry.getSourcePermanentId(), entry.getControllerId(),
                        false, e.exileInsteadOfGraveyard(), e.additionalGenericCost()));

        gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " allows ", targetCard,
                " to be cast from a graveyard."));
        log.info("Game {} - {} grants graveyard cast permission for {}",
                gameData.id, entry.getCard().getName(), targetCard.getName());
    }
}
