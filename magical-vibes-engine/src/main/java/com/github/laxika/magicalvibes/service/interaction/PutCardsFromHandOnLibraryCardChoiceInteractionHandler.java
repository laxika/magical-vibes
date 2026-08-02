package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Prompts the player to choose the hand cards they will put on top/bottom of their library
 * (Dream Cache's "put two cards from your hand"). On answer, begins the follow-up
 * {@link PendingInteraction.PutCardsFromHandOnLibraryDestinationChoice} for the top/bottom pick.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutCardsFromHandOnLibraryCardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.PutCardsFromHandOnLibraryCardChoice> {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final InputCompletionService inputCompletionService;
    private final GameLogService gameLogService;

    @Override
    public Class<PendingInteraction.PutCardsFromHandOnLibraryCardChoice> handledType() {
        return PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.PutCardsFromHandOnLibraryCardChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice to make");
        }
        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();

        // Keep only valid, unique picks, capped at the allowed count.
        List<UUID> validated = new ArrayList<>();
        for (UUID id : chosenIds) {
            if (interaction.validCardIds().contains(id) && !validated.contains(id)) {
                validated.add(id);
            }
            if (validated.size() >= interaction.maxCount()) {
                break;
            }
        }

        // The shuffle-in variant is mandatory, so an empty or short answer falls back to the
        // first legal cards instead of skipping the requirement (Lat-Nam's Legacy).
        if (interaction.shuffleIn()) {
            for (UUID id : interaction.validCardIds()) {
                if (validated.size() >= interaction.maxCount()) {
                    break;
                }
                if (!validated.contains(id)) {
                    validated.add(id);
                }
            }
        }

        if (validated.size() < interaction.minCount()) {
            for (UUID id : interaction.validCardIds()) {
                if (validated.size() >= interaction.minCount()) {
                    break;
                }
                if (!validated.contains(id)) {
                    validated.add(id);
                }
            }
        }

        gameData.interaction.clearAwaitingInput();

        if (validated.isEmpty()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (interaction.shuffleIn()) {
            shuffleIntoLibrary(gameData, player, validated);
            if (interaction.thenEffect() != null && interaction.thenEffectSourceCard() != null) {
                Card sourceCard = interaction.thenEffectSourceCard();
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        sourceCard,
                        player.getId(),
                        sourceCard.getName() + "'s effect",
                        List.of(interaction.thenEffect())
                ));
            }
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (interaction.placement() == HandToLibraryPlacement.PLAYER_CHOICE) {
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.PutCardsFromHandOnLibraryDestinationChoice(player.getId(), validated));
            return;
        }

        putOnLibrary(gameData, player, validated, interaction.placement() == HandToLibraryPlacement.TOP);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /** Moves the chosen hand cards into the library and shuffles it. */
    private void shuffleIntoLibrary(GameData gameData, Player player, List<UUID> chosenCardIds) {
        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        List<Card> deck = gameData.playerDecks.get(playerId);

        int moved = 0;
        for (UUID id : chosenCardIds) {
            Card found = hand.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
            if (found != null) {
                hand.remove(found);
                deck.add(found);
                moved++;
            }
        }
        LibraryShuffleHelper.shuffleLibrary(gameData, playerId);

        gameLogService.append(gameData, GameLog.text(player.getUsername() + " shuffles "
                + moved + " card(s) from their hand into their library."));
        log.info("Game {} - {} shuffled {} card(s) from hand into library",
                gameData.id, player.getUsername(), moved);
    }

    /**
     * Moves the chosen hand cards onto the given end of the library. When placed on top the first
     * chosen card ends up nearest the top.
     */
    private void putOnLibrary(GameData gameData, Player player, List<UUID> chosenCardIds, boolean onTop) {
        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        List<Card> deck = gameData.playerDecks.get(playerId);

        List<Card> moving = new ArrayList<>();
        for (UUID id : chosenCardIds) {
            Card found = hand.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
            if (found != null) {
                hand.remove(found);
                moving.add(found);
            }
        }
        if (onTop) {
            for (int i = moving.size() - 1; i >= 0; i--) {
                deck.add(0, moving.get(i));
            }
        } else {
            deck.addAll(moving);
        }

        String end = onTop ? "top" : "bottom";
        gameLogService.append(gameData, GameLog.text(player.getUsername() + " puts "
                + moving.size() + " card(s) on " + end + " of their library."));
        log.info("Game {} - {} put {} card(s) on {} of library",
                gameData.id, player.getUsername(), moving.size(), end);
    }
}
