package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPutCardFromHandOnBottomOfLibraryThenDrawEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves the optional hand-card placement and conditional draw. */
@Slf4j
@Component
@RequiredArgsConstructor
public class MayPutCardFromHandOnBottomOfLibraryThenDrawEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPutCardFromHandOnBottomOfLibraryThenDrawEffect.class;
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
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.PutCardsFromHandOnLibraryCardChoice(
                controllerId, validCardIds, handSnapshot, 0, 1, HandToLibraryPlacement.BOTTOM,
                false, entry.getCard(), new DrawCardEffect(1), false));
        log.info("Game {} - {} may put a card from hand on the bottom of their library for {}",
                gameData.id, gameData.playerIdToName.get(controllerId), entry.getCard().getName());
    }
}
