package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandPermanentToBattlefieldOrHandEffect;
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
public class ExileTopUntilNonlandPermanentToBattlefieldOrHandEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopUntilNonlandPermanentToBattlefieldOrHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card hit = null;
        int exiledCount = 0;
        while (!deck.isEmpty()) {
            Card top = deck.removeFirst();
            exileService.exileCard(gameData, controllerId, top);
            exiledCount++;
            if (!top.hasType(CardType.LAND)
                    && top.getType() != null
                    && top.getType().isPermanentType()) {
                hit = top;
                break;
            }
        }

        gameLogService.append(gameData, GameLog.text(
                playerName + " exiles " + exiledCount + " card"
                        + (exiledCount == 1 ? "" : "s") + " from the top of their library ("
                        + sourceName + ")."));

        if (hit == null) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " exiles their entire library without finding a nonland permanent card ("
                            + sourceName + ")."));
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), controllerId, List.of(effect),
                sourceName + " — Put " + hit.getName() + " onto the battlefield? If you don't, put it into your hand.",
                hit.getId()));
        log.info("Game {} - {} exiled {} cards with {} and may put {} onto the battlefield",
                gameData.id, playerName, exiledCount, sourceName, hit.getName());
    }
}
