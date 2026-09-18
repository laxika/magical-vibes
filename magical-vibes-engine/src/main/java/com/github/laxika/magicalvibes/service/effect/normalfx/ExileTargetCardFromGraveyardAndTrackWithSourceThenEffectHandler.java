package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndTrackWithSourceThenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileTargetCardFromGraveyardAndTrackWithSourceThenEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCardFromGraveyardAndTrackWithSourceThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effectToResolve) {
        var effect = (ExileTargetCardFromGraveyardAndTrackWithSourceThenEffect) effectToResolve;
        UUID targetCardId = entry.getTargetCardIds().isEmpty()
                ? entry.getTargetId() : entry.getTargetCardIds().getFirst();
        Card targetCard = targetCardId == null
                ? null : gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target is no longer in a graveyard)."));
            return;
        }

        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCardId);
        if (graveyardOwnerId == null
                || !effect.scope().graveyardOwners(gameData.orderedPlayerIds, entry.getControllerId())
                .contains(graveyardOwnerId)
                || effect.filter() != null && !predicateEvaluationService.matchesCardPredicate(
                targetCard, effect.filter(), entry.getCard().getId(), gameData, graveyardOwnerId)) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target no longer matches its restriction)."));
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, targetCardId);
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            exileService.exileCard(gameData, graveyardOwnerId, targetCard);
        } else {
            exileService.exileCard(gameData, graveyardOwnerId, targetCard, sourcePermanentId);
        }
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(entry.getControllerId()) + " exiles ", targetCard,
                " from a graveyard."));

        int effectIndex = entry.getEffectsToResolve().indexOf(effectToResolve);
        if (effectIndex < 0) {
            throw new IllegalStateException("Could not locate graveyard exile effect on stack entry");
        }
        entry.insertEffectsToResolve(effectIndex + 1, List.of(effect.thenEffect()));
    }
}
