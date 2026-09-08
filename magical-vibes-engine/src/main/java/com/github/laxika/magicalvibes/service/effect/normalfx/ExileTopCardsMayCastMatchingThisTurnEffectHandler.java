package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayCastMatchingThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardsMayCastMatchingThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsMayCastMatchingThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTopCardsMayCastMatchingThisTurnEffect e = (ExileTopCardsMayCastMatchingThisTurnEffect) effect;
        if (e.count() <= 0) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String controllerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(controllerName + "'s library is empty — nothing to exile."));
            return;
        }

        List<String> castableNames = new ArrayList<>();
        List<Card> exiled = new ArrayList<>();
        for (int i = 0; i < e.count() && !deck.isEmpty(); i++) {
            Card topCard = deck.removeFirst();
            exileService.exileCard(gameData, controllerId, topCard);
            exiled.add(topCard);

            if (predicateEvaluationService.matchesCardPredicate(
                    topCard, e.filter(), null, gameData, controllerId)) {
                gameData.exilePlayPermissions.put(topCard.getId(), controllerId);
                gameData.exilePlayPermissionsExpireEndOfTurn.add(topCard.getId());
                if (e.withoutPayingManaCost()) {
                    gameData.exilePlayWithoutPayingManaCost.add(topCard.getId());
                }
                castableNames.add(topCard.getName());
            }
        }

        String castNote = castableNames.isEmpty()
                ? ""
                : " (may cast this turn"
                        + (e.withoutPayingManaCost() ? " without paying mana costs" : "")
                        + ": " + String.join(", ", castableNames) + ")";
        GameLog.Builder logEntry = GameLog.builder().text(controllerName + " exiles ");
        for (int i = 0; i < exiled.size(); i++) {
            if (i > 0) {
                logEntry.text(", ");
            }
            logEntry.card(exiled.get(i));
        }
        gameLogService.append(gameData, logEntry
                .text(" from the top of their library" + castNote + ".").build());
        log.info("Game {} - {} exiles {} cards from library top ({} castable this turn)",
                gameData.id, controllerName, exiled.size(), castableNames.size());
    }
}
