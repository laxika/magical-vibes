package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleCardsFromHandIntoLibraryThenEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Begins the mandatory "shuffle N cards from your hand into your library. If you do, [effect]"
 * choice (Lat-Nam's Legacy). An empty hand means nothing is shuffled and the rider never happens.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShuffleCardsFromHandIntoLibraryThenEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleCardsFromHandIntoLibraryThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ShuffleCardsFromHandIntoLibraryThenEffect) effect;
        UUID controllerId = entry.getControllerId();

        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            log.info("Game {} - {} has no cards to shuffle in for {}",
                    gameData.id, gameData.playerIdToName.get(controllerId), entry.getCard().getName());
            return;
        }

        int count = Math.min(e.count(), hand.size());
        List<Card> handSnapshot = List.copyOf(hand);
        List<UUID> validCardIds = handSnapshot.stream().map(Card::getId).toList();

        interactionHandlerRegistry.begin(gameData, PendingInteraction.PutCardsFromHandOnLibraryCardChoice
                .shuffleIntoLibrary(controllerId, validCardIds, handSnapshot, count,
                        entry.getCard(), e.thenEffect()));

        log.info("Game {} - {} choosing {} card(s) from hand to shuffle into library",
                gameData.id, gameData.playerIdToName.get(controllerId), count);
    }
}
