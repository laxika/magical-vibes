package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPlayFreeEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfTargetPlayerMayCastFreeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardOfTargetPlayerMayCastFreeEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardOfTargetPlayerMayCastFreeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) return;
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String targetPlayerName = gameData.playerIdToName.get(targetPlayerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    targetPlayerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card topCard = deck.getFirst();
        gameLogService.append(gameData, GameLog.builder()
                .text(targetPlayerName + " reveals ")
                .card(topCard)
                .text(" from the top of their library (" + sourceName + ").")
                .build());
        log.info("Game {} - {} reveals top card: {} ({})", gameData.id, targetPlayerName,
                topCard.getName(), sourceName);

        if (topCard.hasType(CardType.LAND)) {
            return;
        }

        RevealTopCardMayPlayFreeEffect freeCastEffect =
                new RevealTopCardMayPlayFreeEffect(LookDestination.TOP_OF_LIBRARY, false, targetPlayerId);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                topCard,
                entry.getControllerId(),
                List.of(freeCastEffect),
                sourceName + " — Cast " + topCard.getName() + " without paying its mana cost?"));
    }
}
