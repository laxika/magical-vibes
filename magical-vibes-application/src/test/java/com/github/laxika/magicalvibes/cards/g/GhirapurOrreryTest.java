package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GhirapurOrreryTest extends BaseCardTest {

    @Test
    @DisplayName("Each player with an empty hand draws three cards during their upkeep")
    void emptyActivePlayerDrawsThreeCards() {
        harness.addToBattlefield(player1, new GhirapurOrrery());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The active player draws during an opponent's upkeep")
    void opponentUpkeepDrawsForActivePlayer() {
        harness.addToBattlefield(player1, new GhirapurOrrery());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("An active player with cards in hand does not draw")
    void nonEmptyActivePlayerDoesNotDraw() {
        harness.addToBattlefield(player1, new GhirapurOrrery());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore);
    }

    @Test
    @DisplayName("The empty-hand condition is checked again when the trigger resolves")
    void emptyHandConditionIsCheckedAtResolution() {
        harness.addToBattlefield(player1, new GhirapurOrrery());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        gd.playerHands.get(player1.getId()).add(new AngelOfMercy());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Each player may play an additional land each turn")
    void raisesLandPlayLimitForEachPlayer() {
        harness.addToBattlefield(player1, new GhirapurOrrery());

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(2);
    }
}
