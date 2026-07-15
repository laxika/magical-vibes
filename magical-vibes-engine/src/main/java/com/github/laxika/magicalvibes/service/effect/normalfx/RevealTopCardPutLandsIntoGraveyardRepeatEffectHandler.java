package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutLandsIntoGraveyardRepeatEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Reveals the controller's library one card at a time; each revealed land card is put into their
 * graveyard and the process repeats. Stops at the first non-land card (which stays on top) or when
 * the library is empty. Used by Countryside Crusher.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardPutLandsIntoGraveyardRepeatEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardPutLandsIntoGraveyardRepeatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        int landsBinned = 0;
        while (deck != null && !deck.isEmpty()) {
            Card topCard = deck.getFirst();
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " reveals " + topCard.getName() + " from the top of their library."));

            if (!topCard.hasType(CardType.LAND)) {
                break;
            }

            deck.removeFirst();
            graveyardService.addCardToGraveyard(gameData, controllerId, topCard);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " puts " + topCard.getName() + " into their graveyard."));
            landsBinned++;
        }

        log.info("Game {} - {} reveal-repeat put {} land(s) into {}'s graveyard",
                gameData.id, sourceName, landsBinned, playerName);
    }
}
