package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleAnyNumberCardsFromHandIntoLibraryThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves a controller's choice to shuffle any number of hand cards and draw that many. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ShuffleAnyNumberCardsFromHandIntoLibraryThenDrawThatManyEffectHandler
        implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleAnyNumberCardsFromHandIntoLibraryThenDrawThatManyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);

        if (gameData.chosenXValue != null) {
            int chosenCount = Math.min(gameData.chosenXValue, hand == null ? 0 : hand.size());
            gameData.chosenXValue = null;
            if (chosenCount == 0) {
                LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
                return;
            }

            List<Card> handSnapshot = List.copyOf(hand);
            List<UUID> validCardIds = handSnapshot.stream().map(Card::getId).toList();
            interactionHandlerRegistry.begin(gameData, PendingInteraction.PutCardsFromHandOnLibraryCardChoice
                    .shuffleIntoLibrary(controllerId, validCardIds, handSnapshot, chosenCount,
                            entry.getCard(), new DrawCardEffect(chosenCount)));
            log.info("Game {} - {} choosing {} card(s) to shuffle into their library for {}",
                    gameData.id, gameData.playerIdToName.get(controllerId), chosenCount, entry.getCard().getName());
            return;
        }

        if (hand == null || hand.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            log.info("Game {} - {} has no cards to shuffle into their library for {}",
                    gameData.id, gameData.playerIdToName.get(controllerId), entry.getCard().getName());
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                controllerId,
                hand.size(),
                "Choose how many cards to shuffle from your hand into your library for "
                        + entry.getCard().getName() + ".",
                entry.getCard().getName()));
    }
}
