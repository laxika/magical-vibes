package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsExileRandomRestOnTopEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link LookAtTopCardsExileRandomRestOnTopEffect}: takes the top N cards of the
 * controller's library, exiles a random subset of them, then puts the remainder back on top in an
 * order the controller chooses (asynchronous {@code LibraryReorder} when two or more remain).
 * Used by Orcish Librarian.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardsExileRandomRestOnTopEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final ExileService exileService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsExileRandomRestOnTopEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (LookAtTopCardsExileRandomRestOnTopEffect) effect;

        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, e.count(), true);
        if (result == null) return; // empty library — already logged

        UUID controllerId = result.controllerId();
        List<Card> looked = result.topCards();

        // Fewer cards than the exile count simply means every looked-at card is exiled.
        int toExile = Math.min(e.exileCount(), looked.size());
        for (int i = 0; i < toExile; i++) {
            Card exiled = looked.remove(ThreadLocalRandom.current().nextInt(looked.size()));
            exileService.exileCard(gameData, controllerId, exiled);
            gameLogService.append(gameData, GameLog.textCardText(
                    result.playerName() + " exiles ", exiled, " at random."));
        }

        if (looked.isEmpty()) {
            return;
        }
        if (looked.size() == 1) {
            gameData.playerDecks.get(controllerId).addFirst(looked.getFirst());
            return;
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                controllerId, looked, false, controllerId,
                "Put these cards on top of your library in any order (top to bottom)."));

        log.info("Game {} - {} exiled {} of {} looked-at cards at random",
                gameData.id, result.playerName(), toExile, e.count());
    }
}
