package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TombstoneStairwellTest extends BaseCardTest {

    /** Advances to player2's upkeep so only the each-upkeep trigger fires (no cumulative upkeep prompt). */
    private void advanceToOpponentUpkeep() {
        advanceToUpkeep(player2);
        harness.passBothPriorities();
    }

    private void advanceToEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to END_STEP, trigger fires onto stack
        harness.passBothPriorities(); // resolve the trigger
    }

    @Test
    @DisplayName("Each player creates a Tombspawn for each creature card in their own graveyard")
    void createsTombspawnPerCreatureCardInGraveyard() {
        harness.addToBattlefield(player1, new TombstoneStairwell());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        advanceToOpponentUpkeep();

        assertThat(countPermanents(player1, "Tombspawn")).isEqualTo(2);
        assertThat(countPermanents(player2, "Tombspawn")).isEqualTo(1);
    }

    @Test
    @DisplayName("Tombspawn tokens have haste")
    void tombspawnHasHaste() {
        harness.addToBattlefield(player1, new TombstoneStairwell());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToOpponentUpkeep();

        Permanent tombspawn = findPermanents(player1, "Tombspawn").getFirst();
        assertThat(gqs.hasKeyword(gd, tombspawn, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("A player with no creature cards in their graveyard creates nothing")
    void noCreatureCardsNoTokens() {
        harness.addToBattlefield(player1, new TombstoneStairwell());
        harness.setGraveyard(player1, List.of(new Shock()));

        advanceToOpponentUpkeep();

        assertThat(countPermanents(player1, "Tombspawn")).isZero();
        assertThat(countPermanents(player2, "Tombspawn")).isZero();
    }

    @Test
    @DisplayName("All Tombspawn tokens are destroyed at the beginning of the end step")
    void destroysTokensAtEndStep() {
        harness.addToBattlefield(player1, new TombstoneStairwell());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        advanceToOpponentUpkeep();
        assertThat(countPermanents(player1, "Tombspawn")).isEqualTo(2);

        advanceToEndStep();

        assertThat(countPermanents(player1, "Tombspawn")).isZero();
        assertThat(countPermanents(player2, "Tombspawn")).isZero();
    }

    @Test
    @DisplayName("Tokens are destroyed when Tombstone Stairwell leaves the battlefield")
    void destroysTokensWhenStairwellLeaves() {
        harness.addToBattlefield(player1, new TombstoneStairwell());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        advanceToOpponentUpkeep();
        assertThat(countPermanents(player1, "Tombspawn")).isEqualTo(1);
        assertThat(countPermanents(player2, "Tombspawn")).isEqualTo(1);

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, findPermanents(player1, "Tombstone Stairwell").getFirst().getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tombstone Stairwell");
        assertThat(countPermanents(player1, "Tombspawn")).isZero();
        assertThat(countPermanents(player2, "Tombspawn")).isZero();
    }
}
