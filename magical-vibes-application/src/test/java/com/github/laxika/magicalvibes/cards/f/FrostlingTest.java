package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BileUrchin;
import com.github.laxika.magicalvibes.cards.g.GoblinCohort;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Frostling.class, BileUrchin.class, GoblinCohort.class})
class FrostlingTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to target creature, killing a 1/1")
    void dealsDamageKillingSmallCreature() {
        harness.addToBattlefield(player1, new Frostling());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BileUrchin());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Bile Urchin");
        harness.assertInGraveyard(player2, "Bile Urchin");
    }

    @Test
    @DisplayName("1 damage is not lethal to a 2/2")
    void leavesLargerCreatureAlive() {
        harness.addToBattlefield(player1, new Frostling());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinCohort());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Goblin Cohort");
    }

    @Test
    @DisplayName("Can target a creature its controller controls")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new Frostling());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BileUrchin());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bile Urchin");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player1, new Frostling());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Frostling");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Frostling is sacrificed as a cost of the ability")
    void sacrificedAsCost() {
        harness.addToBattlefield(player1, new Frostling());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinCohort());

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Frostling");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }
}
