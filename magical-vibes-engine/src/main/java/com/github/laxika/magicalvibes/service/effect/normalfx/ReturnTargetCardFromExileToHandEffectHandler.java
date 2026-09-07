package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardFromExileToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReturnTargetCardFromExileToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCardFromExileToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnTargetCardFromExileToHandEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(e);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        UUID targetId = targetIds.stream().findFirst().orElse(null);
        if ((entry.getTargetZone() != null && entry.getTargetZone() != Zone.EXILE) || targetId == null) {
            String fizzleLog = entry.getDescription() + " fizzles (no valid exile target).";
            gameLogService.append(gameData, GameLog.text(fizzleLog));
            return;
        }

        Card targetCard = gameQueryService.findCardInExileById(gameData, targetId);
        var targetEntry = gameData.findExiledCard(targetId);
        String filterLabel = CardPredicateUtils.describeFilter(e.filter());

        if (targetCard == null || targetEntry == null) {
            String fizzleLog = entry.getDescription() + " fizzles (target " + filterLabel + " is no longer in exile).";
            gameLogService.append(gameData, GameLog.text(fizzleLog));
            return;
        }

        if (e.filter() != null && !predicateEvaluationService.matchesCardPredicate(targetCard, e.filter(), null)) {
            String fizzleLog = entry.getDescription() + " fizzles (target is not a " + filterLabel + ").";
            gameLogService.append(gameData, GameLog.text(fizzleLog));
            return;
        }

        // Remove card from exile
        
        gameData.removeFromExile(targetCard.getId());

        // Put into owner's hand
        gameData.playerHands.get(targetEntry.ownerId()).add(targetCard);

        gameLogService.append(gameData, GameLog.textCardText(entry.getDescription() + " returns " , targetCard, " from exile to hand."));
    }
}
