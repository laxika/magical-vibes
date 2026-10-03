package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRevealsTopCardAndCreatesTokenCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerRevealsTopCardAndCreatesTokenCopyEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerRevealsTopCardAndCreatesTokenCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typed = (EachPlayerRevealsTopCardAndCreatesTokenCopyEffect) effect;
        String sourceName = entry.getCard().getName();
        List<Card> creatureCards = new ArrayList<>();

        for (UUID playerId : apnapOrder(gameData)) {
            List<Card> library = gameData.playerDecks.getOrDefault(playerId, List.of());
            String playerName = gameData.playerIdToName.get(playerId);

            if (library.isEmpty()) {
                gameLogService.append(gameData,
                        GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
                continue;
            }

            Card topCard = library.getFirst();
            gameLogService.append(gameData, GameLog.builder()
                    .text(playerName + " reveals ")
                    .card(topCard)
                    .text(" from the top of their library (" + sourceName + ").")
                    .build());

            if (topCard.hasType(CardType.CREATURE)) {
                creatureCards.add(topCard);
            }
        }

        if (creatureCards.isEmpty()) {
            entry.setReturnToHandAfterResolving(true);
            return;
        }

        tokenCopySupport.createTokenCopies(gameData, entry, creatureCards, null,
                entry.getControllerId(), typed.tokenCopyEffect());
        log.info("Game {} - {} created {} token copies from revealed creatures",
                gameData.id, sourceName, creatureCards.size());
    }

    private List<UUID> apnapOrder(GameData gameData) {
        List<UUID> order = new ArrayList<>();
        if (gameData.activePlayerId != null) {
            order.add(gameData.activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!order.contains(playerId)) {
                order.add(playerId);
            }
        }
        return order;
    }
}
