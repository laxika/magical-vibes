package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShuffleIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // CR 707.10a — copies cease to exist when they leave the stack; they never
        // enter any zone.  The spell disposition handler in StackResolutionService
        // already guards against this, but the effect handler fires first during
        // normal effect resolution, so we need the guard here too.
        if (entry.isCopy()) return;

        // When an earlier effect paused resolution for user input (e.g. Beacon of Unrest's
        // graveyard choice), handleSpellDisposition already shuffled the card in — this
        // handler then runs again on resumption, so it must not add a second copy.
        Card physicalCard = entry.getPhysicalCard();
        List<Card> deck = gameData.playerDecks.get(entry.getOwnerId());
        if (deck.contains(physicalCard)) return;

        deck.add(physicalCard);
        LibraryShuffleHelper.shuffleLibrary(gameData, entry.getOwnerId());

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " is shuffled into its owner's library."));
    }
}
