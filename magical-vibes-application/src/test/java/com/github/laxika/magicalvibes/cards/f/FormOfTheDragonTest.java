package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FormOfTheDragonTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    private void beginAttack(Player attacker) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    // ===== Upkeep: 5 damage to any target =====

    @Test
    @DisplayName("Upkeep trigger deals 5 damage to a chosen player")
    void upkeepDealsFiveToPlayer() {
        harness.addToBattlefield(player1, new FormOfTheDragon());
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Upkeep trigger can deal 5 damage to a creature, destroying it")
    void upkeepDealsFiveToCreature() {
        harness.addToBattlefield(player1, new FormOfTheDragon());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // resolve trigger

        assertThat(gqs.findPermanentById(gd, bears.getId())).isNull();
    }

    // ===== End step: your life total becomes 5 =====

    @Test
    @DisplayName("End step lowers controller's life total to 5")
    void endStepLowersLifeToFive() {
        harness.addToBattlefield(player1, new FormOfTheDragon());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to END_STEP, trigger fires onto stack
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(5);
    }

    @Test
    @DisplayName("End step raises controller's life total up to 5")
    void endStepRaisesLifeToFive() {
        harness.addToBattlefield(player1, new FormOfTheDragon());
        harness.setLife(player1, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to END_STEP, trigger fires onto stack
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(5);
    }

    // ===== Static: creatures without flying can't attack you =====

    @Test
    @DisplayName("Non-flying creature can't attack the controller")
    void nonFlyerCantAttackController() {
        harness.addToBattlefield(player2, new FormOfTheDragon());
        addCreatureReady(player1, new GrizzlyBears()); // ground creature, index 0

        beginAttack(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't attack");
    }

    @Test
    @DisplayName("Flying creature can attack the controller")
    void flyerCanAttackController() {
        harness.addToBattlefield(player2, new FormOfTheDragon());
        addCreatureReady(player1, new SuntailHawk()); // flyer, index 0

        beginAttack(player1);

        // The call not throwing proves the flyer may attack the Form of the Dragon controller.
        gs.declareAttackers(gd, player1, List.of(0));
    }

    @Test
    @DisplayName("Restriction is defender-scoped: controller's own non-flyers can still attack")
    void restrictionOnlyProtectsController() {
        // Player1 controls Form of the Dragon and a ground creature; player2 has no restriction.
        harness.addToBattlefield(player1, new FormOfTheDragon()); // index 0
        addCreatureReady(player1, new GrizzlyBears());            // index 1

        beginAttack(player1);

        // Attacking player2 (who does not control Form of the Dragon) succeeds.
        gs.declareAttackers(gd, player1, List.of(1));
    }
}
