package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinFirestarter.class, JaceBeleren.class})
class GoblinFirestarterTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and deals 1 damage to target player")
    void deals1DamageToPlayer() {
        harness.setLife(player2, 20);
        setupFirestarterOnMyTurn(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertInGraveyard(player1, "Goblin Firestarter");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertNotOnBattlefield(player1, "Goblin Firestarter");
    }

    @Test
    @DisplayName("Deals 1 damage to target planeswalker")
    void deals1DamageToPlaneswalker() {
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        setupFirestarterOnMyTurn(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, jace.getId());
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Goblin Firestarter");
    }

    @Test
    @DisplayName("Deals 1 damage to target creature, destroying a 1/1")
    void deals1DamageDestroying1Toughness() {
        setupFirestarterOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinFirestarter());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Goblin Firestarter");
        harness.assertInGraveyard(player2, "Goblin Firestarter");
    }

    @Test
    @DisplayName("Can activate during beginning of combat, before attackers are declared")
    void canActivateBeforeAttackers() {
        setupFirestarterOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate once attackers have been declared")
    void cannotActivateAfterAttackersDeclared() {
        setupFirestarterOnMyTurn(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate before attackers in a later combat phase")
    void cannotActivateInLaterCombatPhase() {
        setupFirestarterOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);
        gd.combatPhasesThisTurn = 2;

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        harness.addToBattlefield(player1, new GoblinFirestarter());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Firestarter can sacrifice itself during upkeep")
    void canActivateWhileTappedAndSummoningSick() {
        setupFirestarterOnMyTurn(TurnStep.UPKEEP);
        Permanent firestarter = gd.playerBattlefields.get(player1.getId()).getFirst();
        firestarter.tap();
        firestarter.setSummoningSick(true);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertInGraveyard(player1, "Goblin Firestarter");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Can target itself, but the sacrificed target is gone at resolution")
    void canTargetItself() {
        setupFirestarterOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player1, "Goblin Firestarter"));
        harness.assertInGraveyard(player1, "Goblin Firestarter");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A target can sacrifice itself in response, leaving the original ability without a target")
    void targetLeavesBeforeResolution() {
        setupFirestarterOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoblinFirestarter());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 19);
        harness.assertNotOnBattlefield(player1, "Goblin Firestarter");
    }

    @Test
    @DisplayName("Cannot activate in the postcombat main phase even when no creatures attacked")
    void cannotActivateDuringPostcombatMain() {
        setupFirestarterOnMyTurn(TurnStep.POSTCOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");

        harness.assertOnBattlefield(player1, "Goblin Firestarter");
        harness.assertNotInGraveyard(player1, "Goblin Firestarter");
        assertThat(gd.stack).isEmpty();
    }

    private void setupFirestarterOnMyTurn(TurnStep step) {
        harness.addToBattlefield(player1, new GoblinFirestarter());
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }
}
