package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardLandToBattlefieldElseToHandEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardLandToBattlefieldElseToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final BattlefieldEntryService battlefieldEntryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardLandToBattlefieldElseToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        Card topCard = deck.removeFirst();

        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " reveals " + topCard.getName()
                + " from the top of their library (" + sourceName + ")."));

        if (topCard.hasType(CardType.LAND)) {
            Permanent perm = new Permanent(topCard);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.entersBattlefieldUnder(topCard, playerName));

            log.info("Game {} - {} puts {} onto the battlefield ({})",
                    gameData.id, playerName, topCard.getName(), sourceName);
        } else {
            gameData.playerHands.get(controllerId).add(topCard);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " puts " + topCard.getName() + " into their hand (" + sourceName + ")."));
        }
    }
}
