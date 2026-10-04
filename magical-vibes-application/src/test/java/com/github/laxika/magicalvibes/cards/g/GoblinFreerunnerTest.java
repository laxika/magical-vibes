package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinFreerunner.class, GrizzlyBears.class})
class GoblinFreerunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Surge casts for {1}{R} after another spell was cast this turn")
    void surgeUsesAlternateCost() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new GoblinFreerunner()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goblin Freerunner");
    }

    @Test
    @DisplayName("Surge is unavailable before another spell is cast")
    void surgeRequiresAnotherSpellThisTurn() {
        harness.setHand(player1, List.of(new GoblinFreerunner()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The normal mana cost is available without enabling surge")
    void normalCostDoesNotRequireAnotherSpell() {
        harness.setHand(player1, List.of(new GoblinFreerunner()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goblin Freerunner");
    }

    @Test
    @DisplayName("Putting a creature onto the battlefield does not enable surge")
    void enteringWithoutCastingDoesNotEnableSurge() {
        harness.addToBattlefield(player1, new GoblinFreerunner());
        harness.setHand(player1, List.of(new GoblinFreerunner()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A spell cast on a previous turn does not enable surge")
    void surgeResetsAtTurnBoundary() {
        harness.setHand(player1, List.of(new GoblinFreerunner(), new GoblinFreerunner()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Menace rejects a single blocker")
    void menaceRejectsSingleBlocker() {
        addCreatureReady(player1, new GoblinFreerunner());
        addCreatureReady(player2, new GoblinFreerunner());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Menace allows two blockers")
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new GoblinFreerunner());
        Permanent firstBlocker = addCreatureReady(player2, new GoblinFreerunner());
        Permanent secondBlocker = addCreatureReady(player2, new GoblinFreerunner());
        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }
}
