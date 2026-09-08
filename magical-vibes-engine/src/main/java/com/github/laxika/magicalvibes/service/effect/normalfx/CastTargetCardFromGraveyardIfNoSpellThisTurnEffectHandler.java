package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetCardFromGraveyardIfNoSpellThisTurnEffect;
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
public class CastTargetCardFromGraveyardIfNoSpellThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastTargetCardFromGraveyardIfNoSpellThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CastTargetCardFromGraveyardIfNoSpellThisTurnEffect castEffect =
                (CastTargetCardFromGraveyardIfNoSpellThisTurnEffect) effect;
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
        boolean validScope = graveyardOwnerId != null && switch (castEffect.scope()) {
            case OPPONENT_GRAVEYARD -> !graveyardOwnerId.equals(entry.getControllerId());
            case CONTROLLERS_GRAVEYARD -> graveyardOwnerId.equals(entry.getControllerId());
            case ALL_GRAVEYARDS -> true;
        };
        boolean matchesFilter = castEffect.filter() == null
                || predicateEvaluationService.matchesCardPredicate(
                targetCard, castEffect.filter(), entry.getCard().getId());
        if (!validScope || !matchesFilter) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription()
                    + " fizzles (target is no longer legal)."));
            return;
        }

        if (gameData.getSpellsCastThisTurnCount(entry.getControllerId()) > 0) {
            gameLogService.append(gameData, GameLog.cardThen(targetCard,
                    " can't be cast because its controller has already cast a spell this turn."));
            log.info("Game {} - {} cannot cast {} from graveyard because they already cast a spell this turn",
                    gameData.id, gameData.playerIdToName.get(entry.getControllerId()), targetCard.getName());
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                targetCard,
                entry.getControllerId(),
                List.of(castEffect),
                "Cast " + targetCard.getName() + " from your graveyard?"));
        gameLogService.append(gameData, GameLog.cardThen(targetCard,
                " may be cast from the graveyard."));
        log.info("Game {} - {} may cast {} from graveyard",
                gameData.id, gameData.playerIdToName.get(entry.getControllerId()), targetCard.getName());
    }
}
