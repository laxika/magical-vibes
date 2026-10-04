package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShuffleIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Autowired
    public ShuffleIntoLibraryEffectHandler(GameLogService gameLogService,
                                          TriggerCollectionService triggerCollectionService) {
        this.gameLogService = gameLogService;
        this.triggerCollectionService = triggerCollectionService;
    }

    public ShuffleIntoLibraryEffectHandler(GameLogService gameLogService) {
        this(gameLogService, null);
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // The library is shuffled even when the spell cannot be put into it. Flashback's
        // exile replacement is applied by normal spell disposition after resolution.
        if (entry.isCopy() || entry.isCastWithFlashback()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, entry.getOwnerId());
            return;
        }

        // When an earlier effect paused resolution for user input (e.g. Beacon of Unrest's
        // graveyard choice), handleSpellDisposition already shuffled the card in — this
        // handler then runs again on resumption, so it must not add a second copy.
        Card physicalCard = entry.getPhysicalCard();
        List<Card> deck = gameData.playerDecks.get(entry.getOwnerId());
        if (deck.contains(physicalCard)) return;

        deck.add(physicalCard);
        if (triggerCollectionService != null) {
            triggerCollectionService.checkCardsPutIntoLibraryTriggers(gameData, entry.getOwnerId(), 1);
        }
        LibraryShuffleHelper.shuffleLibrary(gameData, entry.getOwnerId());

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " is shuffled into its owner's library."));
    }
}
