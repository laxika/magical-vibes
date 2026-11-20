package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Asks the player the effect names to choose cards from their hand to put on their library, through the
 * {@link PendingInteraction.PutCardsFromHandOnLibraryCardChoice} flow; the entry resumes once they have answered, so the
 * step pauses at its end. Stunted Growth ("target player"), Brainstorm ("you", after its draw step).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TargetPlayerPutsCardsFromHandOnTopOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var put = (TargetPlayerPutsCardsFromHandOnTopOfLibraryEffect) effect;
        UUID playerId = subjectResolutionService.player(gameData, entry, put, put.player());
        if (playerId == null || !gameData.playerIds.contains(playerId)) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand == null || hand.isEmpty()) {
            return;
        }

        int maxCount = Math.min(put.count(), hand.size());
        List<Card> handSnapshot = List.copyOf(hand);
        List<UUID> validCardIds = handSnapshot.stream().map(Card::getId).toList();

        interactionHandlerRegistry.begin(gameData, PendingInteraction.PutCardsFromHandOnLibraryCardChoice
                .putExactlyOnLibrary(playerId, validCardIds, handSnapshot, maxCount, put.placement()));

        log.info("Game {} - {} choosing {} card(s) from hand to put on library ({})",
                gameData.id, gameData.playerIdToName.get(playerId), maxCount, put.placement());
    }
}
