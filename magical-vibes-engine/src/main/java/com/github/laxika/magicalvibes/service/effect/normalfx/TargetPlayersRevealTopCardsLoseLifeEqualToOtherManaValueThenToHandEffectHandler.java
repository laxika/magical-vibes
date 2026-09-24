package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayersRevealTopCardsLoseLifeEqualToOtherManaValueThenToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TargetPlayersRevealTopCardsLoseLifeEqualToOtherManaValueThenToHandEffectHandler
        implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayersRevealTopCardsLoseLifeEqualToOtherManaValueThenToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TargetPlayersRevealTopCardsLoseLifeEqualToOtherManaValueThenToHandEffect revealEffect =
                (TargetPlayersRevealTopCardsLoseLifeEqualToOtherManaValueThenToHandEffect) effect;
        List<UUID> targets = revealEffect.includeController()
                ? entry.getDeclaredTargetIds().isEmpty()
                        ? entry.getTargetId() == null ? List.of() : List.of(entry.getTargetId())
                        : entry.getDeclaredTargetIds()
                : entry.getDeclaredTargetIds();
        if (targets.size() != (revealEffect.includeController() ? 1 : 2)) {
            return;
        }

        String sourceName = entry.getCard().getName();
        List<UUID> playerIds = revealEffect.includeController()
                ? List.of(entry.getControllerId(), targets.getFirst())
                : targets;
        boolean[] legal = revealEffect.includeController()
                ? new boolean[]{true, entry.getDeclaredTargetIds().isEmpty()
                        || entry.isTargetLegal(entry.getDeclaredTargetIds().indexOf(targets.getFirst()))}
                : new boolean[]{entry.isTargetLegal(0), entry.isTargetLegal(1)};
        Card[] revealed = new Card[2];
        for (int i = 0; i < playerIds.size(); i++) {
            if (!legal[i]) {
                continue;
            }

            UUID playerId = playerIds.get(i);
            List<Card> deck = gameData.playerDecks.get(playerId);
            String playerName = gameData.playerIdToName.get(playerId);
            if (deck == null || deck.isEmpty()) {
                gameLogService.append(gameData,
                        GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
                continue;
            }

            Card topCard = deck.removeFirst();
            revealed[i] = topCard;
            gameLogService.append(gameData, GameLog.builder()
                    .text(playerName + " reveals ")
                    .card(topCard)
                    .text(" (mana value " + topCard.getManaValue() + ") from the top of their library ("
                            + sourceName + ").")
                    .build());
        }

        for (int i = 0; i < playerIds.size(); i++) {
            if (!legal[i] || revealed[1 - i] == null) {
                continue;
            }
            int manaValue = revealed[1 - i].getManaValue();
            if (manaValue > 0) {
                lifeSupport.applyLifeLoss(gameData, playerIds.get(i), manaValue, sourceName);
            }
        }

        for (int i = 0; i < playerIds.size(); i++) {
            if (legal[i] && revealed[i] != null) {
                gameData.addCardToHand(playerIds.get(i), revealed[i]);
            }
        }

        log.info("Game {} - target players reveal top cards and put them into hand via {}",
                gameData.id, sourceName);
    }
}
