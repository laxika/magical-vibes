package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Offers the controller a cast of the targeted graveyard card while the ability resolves. The target
 * is rechecked against the scope and filter on resolution; the cast itself is performed by
 * {@code CastTargetCardFromGraveyardHandler} once the controller accepts.
 */
@Component
@RequiredArgsConstructor
public class CastTargetCardFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastTargetCardFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CastTargetCardFromGraveyardEffect) effect;
        UUID controllerId = entry.getControllerId();

        UUID targetCardId = !entry.getTargetCardIds().isEmpty()
                ? entry.getTargetCardIds().getFirst()
                : entry.getTargetId();
        if (targetCardId == null) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription() + " — no target selected."));
            return;
        }

        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        UUID graveyardOwnerId = targetCard == null
                ? null : gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());
        if (targetCard == null || graveyardOwnerId == null) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription()
                    + " fizzles (target no longer in graveyard)."));
            return;
        }
        boolean validScope = switch (e.scope()) {
            case OPPONENT_GRAVEYARD -> !graveyardOwnerId.equals(controllerId);
            case CONTROLLERS_GRAVEYARD -> graveyardOwnerId.equals(controllerId);
            case ALL_GRAVEYARDS -> true;
        };
        if (!validScope || (e.filter() != null
                && !predicateEvaluationService.matchesCardPredicate(targetCard, e.filter(), entry.getCard().getId()))) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription()
                    + " fizzles (target is no longer legal)."));
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                targetCard, controllerId, List.of(e),
                entry.getCard().getName() + " — Cast " + targetCard.getName() + "?",
                entry.getSourcePermanentId(), entry.getTriggeringPermanentPowerAtTrigger(), entry.getXValue()));
    }
}
