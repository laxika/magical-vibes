package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Draw {@code drawCount} cards, then begin a choice of {@code putCount} hand cards to put on top of
 * or on the bottom of the controller's library (the destination is chosen in a follow-up). Dream Cache.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect) effect;
        UUID controllerId = entry.getControllerId();

        playerInteractionSupport.applyDrawCards(gameData, controllerId, e.drawCount());

        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            return;
        }
        int maxCount = Math.min(e.putCount(), hand.size());
        List<Card> handSnapshot = List.copyOf(hand);
        List<UUID> validCardIds = handSnapshot.stream().map(Card::getId).toList();

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.PutCardsFromHandOnLibraryCardChoice(
                controllerId, validCardIds, handSnapshot, maxCount, e.topOnly()));

        log.info("Game {} - {} choosing {} card(s) from hand to put on {} of library",
                gameData.id, gameData.playerIdToName.get(controllerId), maxCount,
                e.topOnly() ? "top" : "top/bottom");
    }
}
