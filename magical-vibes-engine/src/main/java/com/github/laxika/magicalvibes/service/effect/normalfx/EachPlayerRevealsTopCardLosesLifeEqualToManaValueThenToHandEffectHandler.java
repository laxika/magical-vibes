package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRevealsTopCardLosesLifeEqualToManaValueThenToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Duskmantle Seer: each player reveals the top card of their library, loses life equal to its mana value, then puts
 * it into their hand. The instructions are followed in that order (CR 608.2c): every player reveals (the card stays
 * in the library, CR 701.20a), the life losses happen at once (the official ruling: they are simultaneous, so two
 * players reaching 0 draw the game), and only then does each revealed card go to its owner's hand.
 *
 * <p>Marked {@link AsksDeathReplacementsFirst}: nothing changes before the losses, and the reveal can't change what a
 * Lich's Duel Mastery's sacrifice can be replaced with, so every such question (CR 616.1) is asked before anything
 * is revealed and the effect is resolved again with the answers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerRevealsTopCardLosesLifeEqualToManaValueThenToHandEffectHandler
        implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final LifeSupport lifeSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerRevealsTopCardLosesLifeEqualToManaValueThenToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        String sourceName = entry.getCard().getName();

        Map<UUID, Card> revealed = new LinkedHashMap<>();
        Map<UUID, Integer> losses = new LinkedHashMap<>();
        for (UUID playerId : apnapOrder(gameData)) {
            List<Card> deck = gameData.playerDecks.get(playerId);
            if (deck != null && !deck.isEmpty()) {
                Card topCard = deck.getFirst();
                revealed.put(playerId, topCard);
                losses.put(playerId, topCard.getManaValue());
            }
        }
        lifeSupport.askBeforeLifeLossesThatFollowAChange(gameData, losses);

        for (UUID playerId : apnapOrder(gameData)) {
            String playerName = gameData.playerIdToName.get(playerId);
            Card topCard = revealed.get(playerId);
            if (topCard == null) {
                gameLogService.append(gameData,
                        GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
                continue;
            }
            gameLogService.append(gameData, GameLog.builder()
                    .text(playerName + " reveals ")
                    .card(topCard)
                    .text(" (mana value " + topCard.getManaValue() + ") from the top of their library ("
                            + sourceName + ").")
                    .build());
        }

        lifeSupport.applyLifeLosses(gameData, losses, sourceName);

        revealed.forEach((playerId, topCard) -> {
            List<Card> deck = gameData.playerDecks.get(playerId);
            if (deck != null && deck.remove(topCard)) {
                gameData.addCardToHand(playerId, topCard);
                log.info("Game {} - {} reveals {} (MV {}) and puts it into hand via {}",
                        gameData.id, gameData.playerIdToName.get(playerId), topCard.getName(),
                        topCard.getManaValue(), sourceName);
            }
        });
    }

    private List<UUID> apnapOrder(GameData gameData) {
        List<UUID> order = new ArrayList<>();
        order.add(gameData.activePlayerId);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(gameData.activePlayerId)) {
                order.add(playerId);
            }
        }
        return order;
    }
}
