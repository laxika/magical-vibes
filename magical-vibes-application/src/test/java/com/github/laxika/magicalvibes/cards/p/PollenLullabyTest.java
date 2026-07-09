package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PollenLullabyTest extends BaseCardTest {

    private void castPollenLullaby() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new PollenLullaby()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities(); // resolve spell (prevent combat damage + clash)
    }

    // ===== Base effect: prevent all combat damage =====

    @Test
    @DisplayName("Prevents all combat damage after resolving")
    void preventsAllCombatDamage() {
        // Clash outcome is irrelevant to the prevention clause.
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        castPollenLullaby();

        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    // ===== Won clash — opponent's creatures don't untap =====

    @Test
    @DisplayName("Winning the clash freezes the opponent's creatures through their next untap step")
    void wonClashFreezesOpponentCreatures() {
        // Higher mana value on top for player1 (Grizzly Bears MV 2 > Forest MV 0) → player1 wins.
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castPollenLullaby();

        assertThat(opponentCreature.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Winning the clash does not affect the caster's own creatures")
    void wonClashLeavesOwnCreaturesAlone() {
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castPollenLullaby();

        assertThat(ownCreature.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Frozen opponent creature stays tapped through its next untap step")
    void frozenCreatureStaysTapped() {
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentCreature.tap();

        castPollenLullaby();
        assertThat(opponentCreature.getSkipUntapCount()).isEqualTo(1);

        // Advance to player2's untap step — the skip is consumed and the creature stays tapped.
        advanceToNextTurn(player1);
        assertThat(opponentCreature.isTapped()).isTrue();
    }

    // ===== Lost clash — nothing frozen =====

    @Test
    @DisplayName("Losing the clash leaves the opponent's creatures untouched")
    void lostClashDoesNotFreeze() {
        // Lower mana value on top for player1 (Forest MV 0 < Grizzly Bears MV 2) → player1 loses.
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        gd.playerDecks.get(player2.getId()).addFirst(new GrizzlyBears());

        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castPollenLullaby();

        assertThat(opponentCreature.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("A mana value tie is not a win, so nothing is frozen")
    void tiedClashDoesNotFreeze() {
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new GrizzlyBears());

        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castPollenLullaby();

        assertThat(opponentCreature.getSkipUntapCount()).isZero();
    }

    // ===== Helpers =====

    private void advanceToNextTurn(com.github.laxika.magicalvibes.model.Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn
    }
}
