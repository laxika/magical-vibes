package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ScrollRackEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Begins Scroll Rack's hand-for-library-top swap: the controller picks any number of hand cards,
 * and the follow-up interaction chain moves that many cards off the library top into their hand and
 * puts the picked cards back on top in an order of their choosing. An empty hand resolves with no
 * prompt.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScrollRackEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ScrollRackEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            return;
        }

        List<Card> handSnapshot = List.copyOf(hand);
        List<UUID> validCardIds = handSnapshot.stream().map(Card::getId).toList();

        interactionHandlerRegistry.begin(gameData, PendingInteraction.PutCardsFromHandOnLibraryCardChoice
                .swapWithLibraryTop(controllerId, validCardIds, handSnapshot));

        log.info("Game {} - {} choosing hand cards to swap with the top of their library",
                gameData.id, gameData.playerIdToName.get(controllerId));
    }
}
