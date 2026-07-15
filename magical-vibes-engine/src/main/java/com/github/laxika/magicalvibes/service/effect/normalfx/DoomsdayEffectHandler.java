package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoomsdayEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DoomsdayEffect}: the controller loses half their life (rounded up), then their
 * library and graveyard are held out as one combined pool. A {@link PendingInteraction.DoomsdayChoice}
 * lets the controller keep up to five of those cards on top of their library (ordered via the
 * shared library-reorder flow); the rest are exiled.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DoomsdayEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DoomsdayEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        String cardName = entry.getCard().getName();

        // Combine the library and graveyard into a single pool, held out of both zones.
        List<Card> pool = new ArrayList<>();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library != null) {
            pool.addAll(library);
            library.clear();
        }
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            pool.addAll(graveyard);
            graveyard.clear();
        }

        // "You lose half your life, rounded up." Computed from the current life total.
        int life = gameData.getLife(controllerId);
        int lifeLoss = (life + 1) / 2;
        lifeSupport.applyLifeLoss(gameData, controllerId, lifeLoss, cardName);

        if (pool.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(controllerName + " has no cards in their library or graveyard (" + cardName + ")."));
            return;
        }

        int maxCount = Math.min(5, pool.size());
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.DoomsdayChoice(controllerId, pool, maxCount));

        log.info("Game {} - Awaiting {} to choose up to {} cards for {} (pool of {})",
                gameData.id, controllerName, maxCount, cardName, pool.size());
    }
}
