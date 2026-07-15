package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTopOfLibraryWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CastTopOfLibraryWithoutPayingManaCostEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastTopOfLibraryWithoutPayingManaCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CastTopOfLibraryWithoutPayingManaCostEffect e = (CastTopOfLibraryWithoutPayingManaCostEffect) effect;

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

        String logEntry = playerName + " looks at the top card of their library (" + sourceName + ").";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} looks at top card: {} ({})", gameData.id, playerName, topCard.getName(), sourceName);

        boolean matches = e.castableTypes().contains(topCard.getType());
        if (!matches) {
            String noMatch = "The top card is not a castable type.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(noMatch));
            log.info("Game {} - Top card {} doesn't match castable types", gameData.id, topCard.getName());
            return;
        }

        // Card matches — queue may ability to cast the spell without paying mana cost
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                topCard,
                controllerId,
                List.of(e),
                sourceName + " — Cast " + topCard.getName() + " without paying its mana cost?"
        ));
    
    }
}
