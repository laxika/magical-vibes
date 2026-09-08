package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutAnyNumberCardsFromHandOnBottomOfLibraryThenDrawThatManyPlusOneEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves the hand-card choice for Into the Fire's redraw mode. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutAnyNumberCardsFromHandOnBottomOfLibraryThenDrawThatManyPlusOneEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutAnyNumberCardsFromHandOnBottomOfLibraryThenDrawThatManyPlusOneEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);

        if (gameData.chosenXValue != null) {
            int chosenCount = Math.min(gameData.chosenXValue, hand == null ? 0 : hand.size());
            gameData.chosenXValue = null;
            if (chosenCount == 0) {
                playerInteractionSupport.applyDrawCards(gameData, controllerId, 1);
                return;
            }

            List<Card> handSnapshot = List.copyOf(hand);
            List<UUID> validCardIds = handSnapshot.stream().map(Card::getId).toList();
            interactionHandlerRegistry.begin(gameData, PendingInteraction.PutCardsFromHandOnLibraryCardChoice
                    .putOnLibraryThenEffect(controllerId, validCardIds, handSnapshot, chosenCount,
                            HandToLibraryPlacement.BOTTOM, entry.getCard(), new DrawCardEffect(chosenCount + 1)));
            log.info("Game {} - {} choosing {} card(s) to put on the bottom of their library for {}",
                    gameData.id, gameData.playerIdToName.get(controllerId), chosenCount, entry.getCard().getName());
            return;
        }

        if (hand == null || hand.isEmpty()) {
            playerInteractionSupport.applyDrawCards(gameData, controllerId, 1);
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                controllerId,
                hand.size(),
                "Choose how many cards to put on the bottom of your library for "
                        + entry.getCard().getName() + ".",
                entry.getCard().getName()));
    }
}
