package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Clue.class, Counterspell.class})
class ClueTest extends BaseCardTest {

    @Test
    @DisplayName("Paying two mana and sacrificing a Clue draws a card")
    void sacrificesAndDrawsCard() {
        Clue clue = new Clue();
        clue.setToken(true);
        harness.addToBattlefield(player1, clue);
        Card drawnCard = new Counterspell();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Clue");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Clue");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("Insufficient mana does not sacrifice the Clue or draw a card")
    void insufficientManaDoesNotPaySacrificeCost() {
        Clue clue = new Clue();
        clue.setToken(true);
        harness.addToBattlefield(player1, clue);
        Card drawnCard = new Counterspell();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Clue");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("A tapped Clue can be sacrificed using colored mana to draw for its controller")
    void tappedClueCanBeActivatedWithColoredMana() {
        Clue clue = new Clue();
        clue.setToken(true);
        harness.addToBattlefieldAndReturn(player2, clue).tap();
        Card drawnCard = new Counterspell();
        harness.setLibrary(player2, List.of(drawnCard));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        int opponentHandSizeBefore = gd.playerHands.get(player1.getId()).size();
        int controllerHandSizeBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Clue");
        harness.assertNotInGraveyard(player2, "Clue");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(controllerHandSizeBefore + 1)
                .contains(drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(opponentHandSizeBefore);
    }
}
