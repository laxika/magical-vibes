package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.effect.TaintedPactCardChoiceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Shared one-card iteration for Tainted Pact's library loop. */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaintedPactSupport {

    private final GameLogService gameLogService;

    /**
     * Exiles the next library card and queues the hand choice when the card does not duplicate a
     * previously exiled name. Returns whether a choice was queued.
     */
    public boolean exileTopCardAndOfferToHand(GameData gameData, Card sourceCard, UUID controllerId,
                                               List<String> exiledNames) {
        List<Card> library = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = sourceCard.getName();

        if (library == null || library.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            log.info("Game {} - {} stops because {}'s library is empty", gameData.id, sourceName, playerName);
            return false;
        }

        Card topCard = library.removeFirst();
        gameData.addToExile(controllerId, topCard);
        gameLogService.append(gameData, GameLog.textCardText(
                playerName + " exiles ", topCard, " from the top of their library (" + sourceName + ")."));

        if (exiledNames.contains(topCard.getName())) {
            gameLogService.append(gameData, GameLog.textCardText(
                    topCard.getName() + " has already been exiled this way, so the process stops (",
                    sourceCard, ")."));
            log.info("Game {} - {} stops on duplicate name {}", gameData.id, sourceName, topCard.getName());
            return false;
        }

        List<String> namesForNextChoice = new ArrayList<>(exiledNames);
        namesForNextChoice.add(topCard.getName());
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                controllerId,
                List.of(new TaintedPactCardChoiceEffect(namesForNextChoice)),
                sourceName + " — Put " + topCard.getName() + " into your hand?",
                topCard.getId()
        ));
        log.info("Game {} - {} offers {} to {}'s hand via {}",
                gameData.id, sourceName, topCard.getName(), playerName, sourceName);
        return true;
    }
}
