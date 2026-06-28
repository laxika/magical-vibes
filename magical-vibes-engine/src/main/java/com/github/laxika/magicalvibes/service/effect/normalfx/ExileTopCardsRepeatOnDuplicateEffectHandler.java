package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsRepeatOnDuplicateEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardsRepeatOnDuplicateEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsRepeatOnDuplicateEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTopCardsRepeatOnDuplicateEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);
        String creatureName = entry.getCard().getName();

        String triggerLog = creatureName + "'s ability triggers — " + playerName + " exiles cards from the top of their library.";
        gameBroadcastService.logAndBroadcast(gameData, triggerLog);

        boolean repeat = true;
        while (repeat) {
            repeat = false;

            if (deck.isEmpty()) {
                String logEntry = playerName + "'s library is empty. No cards to exile.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                break;
            }

            int cardsToExile = Math.min(e.count(), deck.size());
            List<Card> exiledThisRound = new ArrayList<>();
            for (int i = 0; i < cardsToExile; i++) {
                Card card = deck.removeFirst();
                gameData.addToExile(targetPlayerId, card);
                exiledThisRound.add(card);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(playerName).append(" exiles ");
            for (int i = 0; i < exiledThisRound.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(exiledThisRound.get(i).getName());
            }
            sb.append(".");
            gameBroadcastService.logAndBroadcast(gameData, sb.toString());

            Set<String> seen = new HashSet<>();
            for (Card card : exiledThisRound) {
                if (!seen.add(card.getName())) {
                    repeat = true;
                    break;
                }
            }

            if (repeat) {
                String repeatLog = "Two or more exiled cards share the same name — repeating the process.";
                gameBroadcastService.logAndBroadcast(gameData, repeatLog);
            }
        }

        log.info("Game {} - {} exile trigger resolved for {}", gameData.id, creatureName, playerName);
    }
}
