package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillHalfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MillHalfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillHalfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MillHalfLibraryEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        int cardsToMill = e.roundUp() ? (deck.size() + 1) / 2 : deck.size() / 2;
        if (cardsToMill == 0) {
            String logEntry = playerName + "'s library has " + pluralCards(deck.size()) + " — mills nothing.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        graveyardService.resolveMillPlayer(gameData, targetPlayerId, cardsToMill);
    }

    private static String pluralCards(int count) {
        return count + " card" + (count != 1 ? "s" : "");
    }
}
