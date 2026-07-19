package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SuicidalChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing gives opponents' creatures -1/-1 and forces them to attack this turn")
    void weakensAndForcesOpponentCreatures() {
        harness.addToBattlefield(player1, new SuicidalCharge());
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enemyBear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enemyBear)).isEqualTo(1);
        assertThat(enemyBear.isMustAttackThisTurn()).isTrue();
        // The enchantment was sacrificed as the cost.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Suicidal Charge"));
    }

    @Test
    @DisplayName("Leaves the controller's own creatures untouched")
    void doesNotAffectOwnCreatures() {
        harness.addToBattlefield(player1, new SuicidalCharge());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(ownBear.isMustAttackThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The -1/-1 and must-attack wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new SuicidalCharge());
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enemyBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enemyBear)).isEqualTo(2);
        assertThat(enemyBear.isMustAttackThisTurn()).isFalse();
    }
}
