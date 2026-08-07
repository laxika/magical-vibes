package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReorderTopCardsOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReorderTopCardsOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReorderTopCardsOfLibraryEffect reorder = (ReorderTopCardsOfLibraryEffect) effect;

        UUID controllerId = entry.getControllerId();
        UUID deckOwnerId = resolveDeckOwner(entry, reorder.owner(), controllerId);
        List<Card> deck = gameData.playerDecks.get(deckOwnerId);
        String controllerName = gameData.playerIdToName.get(controllerId);
        // "their library" reads correctly only when the decider owns the deck; otherwise name the
        // owner. Derived from the resolved ids rather than the owner axis, so targeting yourself
        // ("target player" includes you) still reads as your own library.
        boolean ownLibrary = deckOwnerId.equals(controllerId);
        String libraryOf = ownLibrary ? "their library" : gameData.playerIdToName.get(deckOwnerId) + "'s library";

        int count = Math.min(reorder.count(), deck.size());
        if (count == 0) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), ownLibrary
                    ? ": library is empty, nothing to reorder."
                    : ": " + gameData.playerIdToName.get(deckOwnerId) + "'s library is empty, nothing to reorder."));
            return;
        }

        if (count == 1) {
            gameLogService.append(gameData,
                    GameLog.text(controllerName + " looks at the top card of " + libraryOf + "."));
            return;
        }

        List<Card> topCards = new ArrayList<>(deck.subList(0, count));
        deck.subList(0, count).clear();

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                controllerId, topCards, false, deckOwnerId,
                "Put these cards back on top of " + (ownLibrary ? "your" : "the")
                        + " library in any order (top to bottom)."));

        gameLogService.append(gameData, GameLog.text(
                controllerName + " looks at the top " + count + " cards of " + libraryOf + "."));
        log.info("Game {} - {} reordering top {} cards of {}", gameData.id, controllerName, count, libraryOf);
    }

    /** Falls back to the controller when a target-player form resolves without a target. */
    private static UUID resolveDeckOwner(StackEntry entry, LibraryOwner owner, UUID controllerId) {
        if (owner != LibraryOwner.TARGET_PLAYER) return controllerId;
        return entry.getTargetId() != null ? entry.getTargetId() : controllerId;
    }
}
