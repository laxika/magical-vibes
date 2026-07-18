package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingReturnExiledWithSourceCard;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardExiledWithSourceIntoHandEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Puts one card the controller owns exiled "with" the source permanent into their hand. With a
 * single exiled card it goes directly to hand; with several, the controller chooses which one via a
 * {@code LIBRARY_REVEAL_CHOICE} routed by {@link PendingReturnExiledWithSourceCard}. Used by Endless
 * Horizons's upkeep trigger (wrapped in {@code MayEffect}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutCardExiledWithSourceIntoHandEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCardExiledWithSourceIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();
        if (sourcePermanentId == null) return;

        List<Card> matching = gameData.exiledCards.stream()
                .filter(e -> sourcePermanentId.equals(e.sourcePermanentId())
                        && controllerId.equals(e.ownerId()))
                .map(com.github.laxika.magicalvibes.model.ExiledCardEntry::card)
                .toList();

        if (matching.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(controllerName + " has no cards exiled with " + sourceName + "."));
            return;
        }

        if (matching.size() == 1) {
            Card card = matching.getFirst();
            gameData.removeFromExile(card.getId());
            gameData.addCardToHand(controllerId, card);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(controllerName + " puts ", card, " from exile into their hand."));
            log.info("Game {} - {} returns {} from exile ({}) to hand",
                    gameData.id, controllerName, card.getName(), sourceName);
            return;
        }

        gameData.queueInteraction(new PendingReturnExiledWithSourceCard());
        List<UUID> validIds = matching.stream().map(Card::getId).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId, new ArrayList<>(matching), validIds,
                false, true, false, false, false, 0, null, 1,
                "Choose a card exiled with " + sourceName + " to put into your hand."));
        gameBroadcastService.broadcastGameState(gameData);

        log.info("Game {} - {} chooses from {} cards exiled with {}",
                gameData.id, controllerName, matching.size(), sourceName);
    }
}
