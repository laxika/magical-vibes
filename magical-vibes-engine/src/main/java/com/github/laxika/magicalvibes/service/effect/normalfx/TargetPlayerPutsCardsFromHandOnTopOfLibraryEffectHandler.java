package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Asks the targeted player to choose cards from their hand to put on top of their library in any
 * order, reusing the {@link PendingInteraction.PutCardsFromHandOnLibraryCardChoice} top-only flow.
 * Stunted Growth.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TargetPlayerPutsCardsFromHandOnTopOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        if (hand == null || hand.isEmpty()) {
            return;
        }

        int maxCount = Math.min(((TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect) effect).count(), hand.size());
        List<Card> handSnapshot = List.copyOf(hand);
        List<UUID> validCardIds = handSnapshot.stream().map(Card::getId).toList();

        interactionHandlerRegistry.begin(gameData, PendingInteraction.PutCardsFromHandOnLibraryCardChoice
                .putExactlyOnLibrary(targetPlayerId, validCardIds, handSnapshot, maxCount,
                        HandToLibraryPlacement.TOP));

        log.info("Game {} - {} choosing {} card(s) from hand to put on top of library",
                gameData.id, gameData.playerIdToName.get(targetPlayerId), maxCount);
    }
}
