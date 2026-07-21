package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AladdinsRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GatherSpecimensTest extends BaseCardTest {

    private void castGatherSpecimens() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GatherSpecimens()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    private String nameOf(Permanent p) {
        return p.getCard().getName();
    }

    @Test
    @DisplayName("A creature spell cast by an opponent enters under your control instead")
    void opponentCreatureEntersUnderYourControl() {
        castGatherSpecimens();

        // Opponent (player2) casts a creature this turn.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        // The creature enters under the Gather Specimens caster's control.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(this::nameOf).contains("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(this::nameOf).doesNotContain("Grizzly Bears");
    }

    @Test
    @DisplayName("Only creatures are redirected — an opponent's noncreature artifact is unaffected")
    void opponentNoncreatureUnaffected() {
        castGatherSpecimens();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new AladdinsRing()));
        harness.addMana(player2, ManaColor.COLORLESS, 8);
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        // The noncreature artifact stays under its caster's control.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(this::nameOf).contains("Aladdin's Ring");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(this::nameOf).doesNotContain("Aladdin's Ring");
    }

    @Test
    @DisplayName("Without Gather Specimens, an opponent's creature stays under their control")
    void baselineOpponentKeepsCreature() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(this::nameOf).contains("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(this::nameOf).doesNotContain("Grizzly Bears");
    }

    @Test
    @DisplayName("Opponent creature tokens are redirected under your control")
    void opponentTokenCreatureRedirected() {
        castGatherSpecimens();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GatherTheTownsfolk()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && "Human".equals(nameOf(p)))
                .count()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().isToken() && "Human".equals(nameOf(p)))
                .count()).isZero();
    }

    @Test
    @DisplayName("Your own creatures still enter under your control")
    void ownCreatureUnaffected() {
        castGatherSpecimens();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(this::nameOf).contains("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(this::nameOf).doesNotContain("Grizzly Bears");
    }

    @Test
    @DisplayName("Effect expires at turn cleanup — next-turn opponent creatures stay theirs")
    void effectExpiresNextTurn() {
        castGatherSpecimens();
        advanceToNextTurn(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(this::nameOf).contains("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(this::nameOf).doesNotContain("Grizzly Bears");
    }

    @Test
    @DisplayName("Both players gathering: creature that would enter under one of them stays with that player (CR 616)")
    void bothPlayersGathering_intendedControllerKeepsCreature() {
        // Player1's Gather Specimens.
        castGatherSpecimens();

        // Player2 also casts Gather Specimens this turn.
        harness.setHand(player2, List.of(new GatherSpecimens()));
        harness.addMana(player2, ManaColor.BLUE, 6);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        // Creature would enter under player2. After both replacements apply once, it stays with player2
        // (ALA Gather Specimens ruling / CR 616).
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(this::nameOf).contains("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(this::nameOf).doesNotContain("Grizzly Bears");
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
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
