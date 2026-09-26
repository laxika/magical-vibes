package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WidespreadPanic.class, GrizzlyBears.class})
class WidespreadPanicTest extends BaseCardTest {

    @Test
    @DisplayName("Makes the player who shuffled put a card from their hand on top of their library")
    void putsShufflingPlayersCardOnTop() {
        Card handCard = new GrizzlyBears();
        Card oldTop = gd.playerDecks.get(player2.getId()).getFirst();
        harness.setHand(player2, List.of(handCard));
        harness.setLibrary(player2, List.of(oldTop));
        harness.addToBattlefield(player1, new WidespreadPanic());

        LibraryShuffleHelper.shuffleLibrary(gd, player2.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class,
                        choice -> {
                            assertThat(choice.playerId()).isEqualTo(player2.getId());
                            assertThat(choice.maxCount()).isEqualTo(1);
                        });

        harness.handleMultipleCardsChosen(player2, List.of(handCard.getId()));

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).startsWith(handCard, oldTop);
    }

    @Test
    @DisplayName("Does not trigger when its controller shuffles their own library")
    void doesNotTriggerOnOwnShuffle() {
        harness.addToBattlefield(player1, new WidespreadPanic());

        LibraryShuffleHelper.shuffleLibrary(gd, player1.getId());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does nothing when the shuffling player has an empty hand")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new WidespreadPanic());

        LibraryShuffleHelper.shuffleLibrary(gd, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
