package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Shared logic for Lim-Dûl's Vault's "look at the top five cards" loop. Used by both the effect
 * handler (the first look) and the repeat/order interaction handlers (each accepted repeat).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LimDulsVaultSupport {

    /** "Look at the top five cards of your library." */
    public static final int LOOK_COUNT = 5;

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    /**
     * Takes the top five cards (or the whole library, if smaller) off {@code controllerId}'s
     * library and begins the repeat prompt over them. Returns {@code false} when the library is
     * empty and there is nothing to look at, in which case nothing was held out or prompted.
     */
    public boolean beginLook(GameData gameData, UUID controllerId) {
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + " has no cards in their library (Lim-Dûl's Vault)."));
            return false;
        }

        List<Card> lookedAt = new ArrayList<>();
        int count = Math.min(LOOK_COUNT, library.size());
        for (int i = 0; i < count; i++) {
            lookedAt.add(library.removeFirst());
        }

        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId) + " looks at the top " + count
                        + " cards of their library (Lim-Dûl's Vault)."));
        log.info("Game {} - {} looks at {} cards for Lim-Dûl's Vault", gameData.id,
                gameData.playerIdToName.get(controllerId), count);

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.LimDulsVaultRepeatChoice(controllerId, lookedAt));
        return true;
    }

    /** Begins the "in any order" prompt for the held-out cards. */
    public void beginOrder(GameData gameData, UUID controllerId, List<Card> cards, boolean toBottom) {
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.LimDulsVaultOrderChoice(controllerId, cards, toBottom));
    }
}
