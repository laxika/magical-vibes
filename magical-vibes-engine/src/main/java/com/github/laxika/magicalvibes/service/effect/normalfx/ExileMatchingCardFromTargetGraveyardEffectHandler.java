package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileMatchingCardFromTargetGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Opens a mandatory resolution-time choice for a matching card in the targeted player's graveyard.
 */
@Component
@RequiredArgsConstructor
public class ExileMatchingCardFromTargetGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileMatchingCardFromTargetGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) {
            return;
        }

        var exileEffect = (ExileMatchingCardFromTargetGraveyardEffect) effect;
        List<Card> candidates = gameData.playerGraveyards
                .getOrDefault(targetPlayerId, List.of())
                .stream()
                .filter(card -> exileEffect.filter() == null
                        || predicateEvaluationService.matchesCardPredicate(
                        card, exileEffect.filter(), entry.getCard().getId()))
                .toList();

        if (candidates.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), " finds no matching card in the target player's graveyard."));
            return;
        }

        gameData.graveyardTargetOperation.resolutionTimeExileResume = true;
        gameData.graveyardTargetOperation.sourcePermanentId = exileEffect.trackWithSource()
                ? entry.getSourcePermanentId() : null;
        int minimumCards = exileEffect.mayChooseNone() ? 0 : 1;
        playerInputService.beginMultiGraveyardChoice(gameData, entry.getControllerId(), candidates, 1,
                minimumCards, "Choose " + (minimumCards == 0 ? "up to one" : "a matching")
                        + " card from the target player's graveyard to exile.");
    }
}
