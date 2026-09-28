package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardMayPlayWhileExiledEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileTargetCardFromGraveyardMayPlayWhileExiledEffectHandler
        implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final ExileService exileService;
    private final ExileSupport exileSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCardFromGraveyardMayPlayWhileExiledEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null && !entry.getTargetCardIds().isEmpty()) {
            targetCardId = entry.getTargetCardIds().getFirst();
        }
        if (targetCardId == null) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription() + " — no target selected."));
            return;
        }

        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target no longer in a graveyard)."));
            return;
        }

        var graveyardEffect = (ExileTargetCardFromGraveyardMayPlayWhileExiledEffect) effect;
        if (graveyardEffect.filter() != null
                && !predicateEvaluationService.matchesCardPredicate(targetCard, graveyardEffect.filter(), null)) {
            String filterLabel = CardPredicateUtils.describeFilter(graveyardEffect.filter());
            gameLogService.append(gameData, GameLog.text(entry.getDescription()
                    + " fizzles (target is no longer a valid " + filterLabel + ")."));
            return;
        }

        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCard.getId());
        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, targetCard.getId());
        UUID ownerId = graveyardOwnerId != null ? graveyardOwnerId : entry.getControllerId();
        exileService.exileCard(gameData, ownerId, targetCard);
        exileSupport.grantPlayWhileExiled(gameData, targetCard.getId(), entry.getControllerId());

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        gameLogService.append(gameData,
                GameLog.textCardText(playerName + " exiles ", targetCard,
                        " from a graveyard (may play while exiled)."));
    }
}
