package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldElseGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardCreatureToBattlefieldElseGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final BattlefieldEntryService battlefieldEntryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardCreatureToBattlefieldElseGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + "'s library is empty (" + sourceName + ").");
            return;
        }

        Card topCard = deck.removeFirst();

        gameBroadcastService.logAndBroadcast(gameData,
                playerName + " reveals " + topCard.getName()
                        + " from the top of their library (" + sourceName + ").");

        if (topCard.hasType(CardType.CREATURE)) {
            Permanent perm = new Permanent(topCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);

            gameBroadcastService.logAndBroadcast(gameData, topCard.getName()
                    + " enters the battlefield under " + playerName + "'s control (" + sourceName + ").");

            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, controllerId, topCard, null, false);

            log.info("Game {} - {} puts {} onto the battlefield ({})",
                    gameData.id, playerName, topCard.getName(), sourceName);
        } else {
            gameData.playerGraveyards.get(controllerId).add(topCard);
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " puts " + topCard.getName() + " into their graveyard (" + sourceName + ").");
        }
    }
}
