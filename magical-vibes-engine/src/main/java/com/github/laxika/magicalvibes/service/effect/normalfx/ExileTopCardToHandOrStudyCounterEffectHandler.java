package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardToHandOrStudyCounterEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardToHandOrStudyCounterEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardToHandOrStudyCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        if (library == null || library.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(controllerName + "'s library is empty — nothing is exiled."));
            return;
        }

        Card topCard = library.removeFirst();
        if (topCard.hasType(CardType.LAND)) {
            gameData.addCardToHand(controllerId, topCard);
            gameLogService.append(gameData, GameLog.textCardText(controllerName + " puts ", topCard,
                    " into their hand from the top of their library."));
        } else {
            exileService.exileCard(gameData, controllerId, topCard);
            gameData.exiledCardsWithStudyCounters.add(topCard.getId());
            gameLogService.append(gameData, GameLog.textCardText(controllerName + " exiles ", topCard,
                    " from the top of their library with a study counter."));
        }
        log.info("Game {} - {} resolves top-card study ability for {}", gameData.id,
                controllerName, topCard.getName());
    }
}
