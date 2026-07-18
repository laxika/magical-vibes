package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldOrMayBottomEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardCreatureToBattlefieldOrMayBottomEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final BattlefieldEntryService battlefieldEntryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardCreatureToBattlefieldOrMayBottomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck.isEmpty()) {
            String logEntry = playerName + "'s library is empty (" + sourceName + ").";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            return;
        }

        Card topCard = deck.getFirst();

        // Reveal the card to all players
        String revealLog = playerName + " reveals " + topCard.getName()
                + " from the top of their library (" + sourceName + ").";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().text(playerName + " reveals ").card(topCard).text(" from the top of their library (" + sourceName + ").").build());

        boolean isCreature = topCard.hasType(CardType.CREATURE);

        if (isCreature) {
            deck.removeFirst();
            Permanent perm = new Permanent(topCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);

            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                    .card(topCard)
                    .text(" enters the battlefield under " + playerName + "'s control (" + sourceName + ").")
                    .build());

            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, controllerId, topCard, null, false);

            log.info("Game {} - {} puts {} onto the battlefield ({})",
                    gameData.id, playerName, topCard.getName(), sourceName);
        } else {
            // Not a creature — ask controller if they want to put it on the bottom
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(), controllerId,
                    List.of(new RevealTopCardCreatureToBattlefieldOrMayBottomEffect()),
                    sourceName + " — Put " + topCard.getName()
                            + " on the bottom of your library?"
            ));
        }
    
    }
}
