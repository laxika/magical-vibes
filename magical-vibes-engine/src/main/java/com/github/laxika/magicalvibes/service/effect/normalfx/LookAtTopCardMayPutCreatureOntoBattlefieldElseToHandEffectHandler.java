package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPutCreatureOntoBattlefieldElseToHandEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Believe: look at the top card; may put a creature onto the battlefield, otherwise put into hand.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardMayPutCreatureOntoBattlefieldElseToHandEffectHandler
        implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayPutCreatureOntoBattlefieldElseToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        // "Look at" is private — do not broadcast the card's identity to opponents.
        gameBroadcastService.logAndBroadcast(gameData,
                GameLog.text(playerName + " looks at the top card of their library (" + sourceName + ")."));

        Card topCard = deck.getFirst();

        if (topCard.hasType(CardType.CREATURE)) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(),
                    controllerId,
                    List.of(new LookAtTopCardMayPutCreatureOntoBattlefieldElseToHandEffect()),
                    sourceName + " — Put " + topCard.getName() + " onto the battlefield?"
            ));
            return;
        }

        // Non-creature: put into hand (no choice).
        deck.removeFirst();
        gameData.playerHands.get(controllerId).add(topCard);
        gameBroadcastService.logAndBroadcast(gameData,
                GameLog.text(playerName + " puts the top card into their hand (" + sourceName + ")."));
        log.info("Game {} - {} puts {} into hand from library top ({})",
                gameData.id, playerName, topCard.getName(), sourceName);
    }
}
