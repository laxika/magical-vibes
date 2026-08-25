package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a targeted graveyard exile and free copy-cast offer. */
@Component
@RequiredArgsConstructor
public class ExileTargetCardFromGraveyardAndMayCastCopyEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final CopySupport copySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCardFromGraveyardAndMayCastCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTargetCardFromGraveyardAndMayCastCopyEffect copyEffect =
                (ExileTargetCardFromGraveyardAndMayCastCopyEffect) effect;
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null && entry.getTargetCardIds() != null
                && !entry.getTargetCardIds().isEmpty()) {
            targetCardId = entry.getTargetCardIds().getFirst();
        }

        Card targetCard = targetCardId == null
                ? null : gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        if (targetCard == null) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target no longer in a graveyard)."));
            return;
        }
        if (copyEffect.filter() != null
                && !predicateEvaluationService.matchesCardPredicate(targetCard, copyEffect.filter(), null)) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription() + " fizzles (target is no longer a valid "
                    + CardPredicateUtils.describeFilter(copyEffect.filter()) + ")."));
            return;
        }

        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, targetCardId);
        if (graveyardOwnerId == null
                || !copyEffect.scope().graveyardOwners(gameData.orderedPlayerIds, entry.getControllerId())
                .contains(graveyardOwnerId)) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target is outside the required graveyard)."));
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, targetCardId);
        exileService.exileCard(gameData, graveyardOwnerId, targetCard);
        gameLogService.append(gameData, GameLog.isExiled(targetCard));

        Card copy = copySupport.createCopyCard(targetCard);
        exileService.exileCard(gameData, entry.getControllerId(), copy);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                copy,
                entry.getControllerId(),
                List.of(copyEffect),
                "Cast the copy of " + copy.getName() + "?",
                copy.getId()));
    }
}
