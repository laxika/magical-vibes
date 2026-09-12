package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPermanentToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardPermanentToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardPermanentToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (library == null || library.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card topCard = library.getFirst();
        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " reveals ")
                .card(topCard)
                .text(" from the top of their library (" + sourceName + ").")
                .build());

        if (!isPermanentCard(topCard)) {
            gameLogService.append(gameData, GameLog.builder()
                    .card(topCard)
                    .text(" remains on top of " + playerName + "'s library (" + sourceName + ").")
                    .build());
            return;
        }

        library.removeFirst();
        Permanent permanent = new Permanent(topCard);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);
        gameLogService.append(gameData, GameLog.entersBattlefieldUnder(topCard, playerName));

        if (topCard.hasType(CardType.CREATURE)) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, topCard, null, false);
        }
        if (topCard.hasType(CardType.PLANESWALKER) && topCard.getLoyalty() != null) {
            permanent.setCounterCount(CounterType.LOYALTY, topCard.getLoyalty());
            permanent.setSummoningSick(false);
        }

        log.info("Game {} - {} puts {} onto the battlefield via {}",
                gameData.id, playerName, topCard.getName(), sourceName);
    }

    private boolean isPermanentCard(Card card) {
        if (card.getType() != null && card.getType().isPermanentType()
                && card.getType() != CardType.KINDRED) {
            return true;
        }
        return card.getAdditionalTypes().stream()
                .anyMatch(type -> type.isPermanentType() && type != CardType.KINDRED);
    }
}
