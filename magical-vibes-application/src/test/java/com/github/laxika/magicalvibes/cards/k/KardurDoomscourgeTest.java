package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KardurDoomscourge.class, GrizzlyBears.class})
class KardurDoomscourgeTest extends BaseCardTest {

    @Test
    @DisplayName("Kardur requires opposing creatures entering later to attack")
    void requiresOpposingCreaturesToAttackUntilNextTurn() {
        castKardur();
        addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Kardur's attack requirements expire at its controller's next turn")
    void attackRequirementsExpireAtControllerNextTurn() {
        castKardur();
        addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        declareAttackers(player2, List.of());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Kardur drains opponents when an attacking creature dies")
    void drainsWhenAttackingCreatureDies() {
        castKardur();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setMarkedDamage(2);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Kardur's attack requirements do not give creatures the goaded designation")
    void attackRequirementsDoNotGoadCreatures() {
        Permanent existingCreature = addCreatureReady(player2, new GrizzlyBears());
        castKardur();
        Permanent laterCreature = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.isGoaded(gd, existingCreature)).isFalse();
        assertThat(gqs.isGoaded(gd, laterCreature)).isFalse();
    }

    @Test
    @DisplayName("Kardur triggers for its own death while attacking")
    void drainsWhenKardurDiesWhileAttacking() {
        castKardur();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent kardur = findPermanent(player1, "Kardur, Doomscourge");
        kardur.setAttacking(true);
        kardur.setMarkedDamage(3);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Kardur triggers for every attacker dying simultaneously with it")
    void drainsForSimultaneousAttackingDeathsIncludingItself() {
        castKardur();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent kardur = findPermanent(player1, "Kardur, Doomscourge");
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        kardur.setAttacking(true);
        kardur.setMarkedDamage(3);
        attacker.setAttacking(true);
        attacker.setMarkedDamage(2);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A nonattacking creature dying does not trigger Kardur")
    void doesNotDrainWhenNonattackingCreatureDies() {
        castKardur();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.setMarkedDamage(2);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Kardur's resolved attack requirements persist after it leaves")
    void attackRequirementsPersistAfterKardurDies() {
        castKardur();
        findPermanent(player1, "Kardur, Doomscourge").setMarkedDamage(3);
        harness.runStateBasedActions();
        resolveAllTriggers();
        addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Kardur does not require its controller's creatures to attack")
    void doesNotRequireOwnCreaturesToAttack() {
        castKardur();
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Kardur does not force a tapped opposing creature to attack")
    void doesNotRequireTappedCreatureToAttack() {
        castKardur();
        addCreatureReady(player2, new GrizzlyBears()).tap();

        declareAttackers(player2, List.of());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castKardur() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new KardurDoomscourge(), "{2}{B}{R}");
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
