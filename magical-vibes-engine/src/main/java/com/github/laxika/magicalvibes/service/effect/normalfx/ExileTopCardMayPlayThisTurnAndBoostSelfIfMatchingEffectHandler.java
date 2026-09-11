package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnAndBoostSelfIfMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves a top-card exile with normal play permission and a conditional self-boost. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardMayPlayThisTurnAndBoostSelfIfMatchingEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final BoostSelfEffectHandler boostSelfEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardMayPlayThisTurnAndBoostSelfIfMatchingEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (ExileTopCardMayPlayThisTurnAndBoostSelfIfMatchingEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String controllerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(controllerName + "'s library is empty - nothing to exile."));
            return;
        }

        Card topCard = deck.removeFirst();
        exileService.exileCard(gameData, controllerId, topCard);
        gameData.exilePlayPermissions.put(topCard.getId(), controllerId);
        gameData.exilePlayPermissionsExpireEndOfTurn.add(topCard.getId());
        gameLogService.append(gameData, GameLog.builder()
                .text(controllerName + " exiles ").card(topCard)
                .text(" from the top of their library (may play it this turn).").build());

        if (predicateEvaluationService.matchesCardPredicate(topCard, exileEffect.matchFilter(), null)) {
            boostSelfEffectHandler.resolve(gameData, entry,
                    new BoostSelfEffect(exileEffect.power(), exileEffect.toughness()));
            log.info("Game {} - {} gets {}/{} from matching exiled card {}", gameData.id,
                    entry.getCard().getName(), exileEffect.power(), exileEffect.toughness(), topCard.getName());
        }
    }
}
