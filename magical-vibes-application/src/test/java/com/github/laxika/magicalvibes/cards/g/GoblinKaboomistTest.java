package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.w.WelkinTern;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinKaboomist.class, RuneclawBear.class, WelkinTern.class})
class GoblinKaboomistTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep creates a Land Mine token and flips a coin")
    void upkeepCreatesLandMineAndFlips() {
        harness.addToBattlefield(player1, new GoblinKaboomist());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(landMineIndex()).isNotNegative();
        assertThat(gameLogContains("coin flip for Goblin Kaboomist")).isTrue();
    }

    @Test
    @DisplayName("Losing the flip deals 2 damage to Goblin Kaboomist, killing it; winning leaves it alone")
    void lostFlipDamagesItself() {
        harness.addToBattlefield(player1, new GoblinKaboomist());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        boolean lost = gameLogContains("loses the coin flip for Goblin Kaboomist");
        boolean stillAlive = countPermanents(player1, "Goblin Kaboomist") != 0;

        // 1/2 creature: 2 damage is lethal, so a lost flip means it is gone.
        assertThat(stillAlive).isEqualTo(!lost);
    }

    @Test
    @DisplayName("Land Mine deals 2 damage to an attacking creature without flying and is sacrificed")
    void landMineDamagesAttacker() {
        harness.addToBattlefield(player1, new GoblinKaboomist());
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player2, new RuneclawBear());
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, landMineIndex(), null, attacker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        assertThat(landMineIndex()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Land Mine cannot target an attacking creature with flying")
    void landMineCannotTargetFlyingAttacker() {
        harness.addToBattlefield(player1, new GoblinKaboomist());
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent flier = addCreatureReady(player2, new WelkinTern());
        flier.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);

        int index = landMineIndex();
        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, flier.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature without flying");
    }

    @Test
    @DisplayName("Land Mine cannot target a creature that is not attacking")
    void landMineCannotTargetNonAttacker() {
        harness.addToBattlefield(player1, new GoblinKaboomist());
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent bystander = addCreatureReady(player2, new RuneclawBear());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);

        int index = landMineIndex();
        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature without flying");
    }

    @Test
    @DisplayName("An opponent's upkeep does not create a Land Mine or flip a coin")
    void opponentUpkeepDoesNotTrigger() {
        harness.addToBattlefield(player1, new GoblinKaboomist());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Land Mine")).isZero();
        assertThat(gameLogContains("coin flip for Goblin Kaboomist")).isFalse();
    }

    @Test
    @DisplayName("The upkeep ability still creates a Land Mine and flips after Kaboomist leaves")
    void upkeepResolvesWithoutKaboomist() {
        Permanent kaboomist = harness.addToBattlefieldAndReturn(player1, new GoblinKaboomist());
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, kaboomist));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Land Mine")).isEqualTo(1);
        assertThat(gameLogContains("coin flip for Goblin Kaboomist")).isTrue();
        harness.assertInGraveyard(player1, "Goblin Kaboomist");
    }

    @Test
    @DisplayName("A Land Mine stays sacrificed when its target stops attacking before resolution")
    void landMineTargetStopsAttacking() {
        harness.addToBattlefield(player1, new GoblinKaboomist());
        advanceToUpkeep(player1);
        resolveAllTriggers();
        Permanent attacker = addCreatureReady(player2, new RuneclawBear());
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, landMineIndex(), null, attacker.getId());
        assertThat(landMineIndex()).isEqualTo(-1);
        attacker.setAttacking(false);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    /** Index of the Land Mine token on player1's battlefield, or -1 when none is there. */
    private int landMineIndex() {
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getName().equals("Land Mine")) {
                return i;
            }
        }
        return -1;
    }
}
