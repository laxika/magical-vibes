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

        if (chosenIds == null
                || chosenIds.size() < interaction.minCount()
                || chosenIds.size() > interaction.maxCount()) {
            throw new IllegalStateException("Invalid number of cards selected");
        }
        List<UUID> validated = new ArrayList<>(chosenIds.size());
        for (UUID id : chosenIds) {
            if (!interaction.validCardIds().contains(id)) {
                throw new IllegalStateException("Invalid card selected");
            }
            if (!validated.add(id)) {
                throw new IllegalStateException("Duplicate card IDs in selection");
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

        if (interaction.swapWithLibraryTop()) {
            swapWithLibraryTop(gameData, player, validated);
            return;
        }

        if (interaction.placement() == HandToLibraryPlacement.PLAYER_CHOICE) {
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.PutCardsFromHandOnLibraryDestinationChoice(player.getId(), validated));
            return;
        }

        putOnLibrary(gameData, player, validated, interaction.placement() == HandToLibraryPlacement.TOP);
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
    }

    /**
     * Scroll Rack: the chosen hand cards are set aside, that many cards are moved from the top of
     * the library into the hand (a move, not a draw — no draw triggers or replacements), and the
     * set-aside cards go back on top through the "in any order" reorder prompt. A single set-aside
     * card has only one legal order, so it skips straight back on top.
     */
    private void swapWithLibraryTop(GameData gameData, Player player, List<UUID> chosenCardIds) {
        UUID playerId = player.getId();
        List<Card> hand = gameData.playerHands.get(playerId);
        List<Card> deck = gameData.playerDecks.get(playerId);

        List<Card> setAside = new ArrayList<>();
        for (UUID id : chosenCardIds) {
            Card found = hand.stream().filter(c -> c.getId().equals(id)).findFirst().orElse(null);
            if (found != null) {
                hand.remove(found);
                setAside.add(found);
            }
        }

        int moved = Math.min(setAside.size(), deck.size());
        for (int i = 0; i < moved; i++) {
            hand.add(deck.remove(0));
        }

        gameLogService.append(gameData, GameLog.text(player.getUsername() + " sets aside " + setAside.size()
                + " card(s) from their hand and puts " + moved + " card(s) from the top of their library"
                + " into their hand."));
        log.info("Game {} - {} swapped {} hand card(s) for {} library card(s)",
                gameData.id, player.getUsername(), setAside.size(), moved);

        if (setAside.size() > 1) {
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(playerId,
                    List.copyOf(setAside), false, playerId,
                    "Put the set-aside cards on top of your library in any order."));
            return;
        }

        deck.add(0, setAside.get(0));
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
