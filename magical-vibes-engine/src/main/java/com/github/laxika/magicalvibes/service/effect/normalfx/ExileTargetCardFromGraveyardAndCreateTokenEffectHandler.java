package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndCreateTokenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileTargetCardFromGraveyardAndCreateTokenEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentControlSupport permanentControlSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCardFromGraveyardAndCreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTargetCardFromGraveyardAndCreateTokenEffect) effect;
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null && !entry.getTargetCardIds().isEmpty()) {
            targetCardId = entry.getTargetCardIds().getFirst();
        }
        Card targetCard = targetCardId == null
                ? null : gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target no longer in a graveyard)."));
            return;
        }

        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());
        if (e.filter() != null && !predicateEvaluationService.matchesCardPredicate(
                targetCard, e.filter(), entry.getCard().getId(), gameData, graveyardOwnerId)) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target no longer matches its restriction)."));
            return;
        }
        if (e.ownGraveyardOnly() && !entry.getControllerId().equals(graveyardOwnerId)) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target is not in your graveyard)."));
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, targetCard.getId());
        UUID sourcePermanentId = e.trackWithSource() ? entry.getSourcePermanentId() : null;
        if (graveyardOwnerId != null) {
            if (sourcePermanentId == null) {
                exileService.exileCard(gameData, graveyardOwnerId, targetCard);
            } else {
                exileService.exileCard(gameData, graveyardOwnerId, targetCard, sourcePermanentId);
            }
        }

        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(entry.getControllerId()) + " exiles ", targetCard,
                " from a graveyard."));
        entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                gameData, entry.getControllerId(), e.tokenTemplate(), entry.getCard().getSetCode()));
    }
}
