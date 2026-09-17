package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AvenFarseer;
import com.github.laxika.magicalvibes.cards.t.TreetopScout;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FormOfTheDragon.class, TreetopScout.class, AvenFarseer.class})
class FormOfTheDragonTest extends BaseCardTest {

    private void resolveEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
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
        Permanent scout = harness.addToBattlefieldAndReturn(player2, new TreetopScout());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, scout.getId());
        harness.passBothPriorities(); // resolve trigger

        assertThat(gqs.findPermanentById(gd, scout.getId())).isNull();
    }

    // ===== End step: your life total becomes 5 =====

    @Test
    @DisplayName("End step lowers controller's life total to 5")
    void endStepLowersLifeToFive() {
        harness.addToBattlefield(player1, new FormOfTheDragon());
        harness.setLife(player1, 20);

        resolveEndStep(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(5);
    }

    @Test
    @DisplayName("End step raises controller's life total up to 5")
    void endStepRaisesLifeToFive() {
        harness.addToBattlefield(player1, new FormOfTheDragon());
        harness.setLife(player1, 2);

        resolveEndStep(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(5);
    }

    @Test
    @DisplayName("End step touches only the controller's life total, not the opponent's")
    void endStepLeavesOpponentLifeAlone() {
        harness.addToBattlefield(player1, new FormOfTheDragon());
        harness.setLife(player1, 20);
        harness.setLife(player2, 13);

        resolveEndStep(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(5);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("End step trigger fires during an opponent's end step")
    void endStepFiresDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new FormOfTheDragon());
        harness.setLife(player1, 20);
        harness.setLife(player2, 13);

        resolveEndStep(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(5);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    // ===== Static: creatures without flying can't attack you =====

    @Test
    @DisplayName("Non-flying creature is not offered when the controller is protected")
    void nonFlyerCantAttackController() {
        harness.addToBattlefield(player2, new FormOfTheDragon());
        addCreatureReady(player1, new TreetopScout()); // ground creature, index 0

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.inMutationScope(() -> harness.getCombatAttackService().handleDeclareAttackersStep(gd));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class))
                .isNull();
    }

    @Test
    @DisplayName("Flying creature can attack the controller")
    void flyerCanAttackController() {
        harness.addToBattlefield(player2, new FormOfTheDragon());
        addCreatureReady(player1, new AvenFarseer()); // flyer, index 0

        // The call not throwing proves the flyer may attack the Form of the Dragon controller.
        declareAttackers(List.of(0));
    }

    @Test
    @DisplayName("Restriction is defender-scoped: controller's own non-flyers can still attack")
    void restrictionOnlyProtectsController() {
        // Player1 controls Form of the Dragon and a ground creature; player2 has no restriction.
        harness.addToBattlefield(player1, new FormOfTheDragon()); // index 0
        addCreatureReady(player1, new TreetopScout());            // index 1

        // Attacking player2 (who does not control Form of the Dragon) succeeds.
        declareAttackers(List.of(1));
    }
}
