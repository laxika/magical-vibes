package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChitteringRatsTest extends BaseCardTest {

    @Test
    @DisplayName("When Chittering Rats enters, the targeted opponent chooses a card to put on top of their library")
    void putsChosenCardOnTopOfOpponentsLibrary() {
        Card chosenCard = new GrizzlyBears();
        Card remainingCard = new Peek();
        Card oldTop = new Swamp();
        harness.setHand(player1, List.of(new ChitteringRats()));
        harness.setHand(player2, new ArrayList<>(List.of(chosenCard, remainingCard)));
        harness.setLibrary(player2, List.of(oldTop));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.getGameService().playCard(gd, player1, 0, 0, player2.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class, choice -> {
                    assertThat(choice.playerId()).isEqualTo(player2.getId());
                    assertThat(choice.maxCount()).isEqualTo(1);
                });

        harness.handleMultipleCardsChosen(player2, List.of(chosenCard.getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remainingCard);
        assertThat(gd.playerDecks.get(player2.getId())).startsWith(chosenCard, oldTop);
        harness.assertOnBattlefield(player1, "Chittering Rats");
    }

    @Test
    @DisplayName("An opponent with an empty hand does not get a card choice")
    void emptyHandDoesNothing() {
        harness.setHand(player1, List.of(new ChitteringRats()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.getGameService().playCard(gd, player1, 0, 0, player2.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Chittering Rats");
    }

    @Test
    @DisplayName("Chittering Rats cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new ChitteringRats()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, player1.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}
