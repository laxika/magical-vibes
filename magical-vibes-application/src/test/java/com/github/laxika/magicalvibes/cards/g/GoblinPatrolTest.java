package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinPatrol.class})
class GoblinPatrolTest extends BaseCardTest {

    @Test
    @DisplayName("Declining echo sacrifices Goblin Patrol at its next upkeep")
    void decliningEchoSacrificesGoblinPatrol() {
        castAndResolveGoblinPatrol();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Goblin Patrol");
        harness.assertInGraveyard(player1, "Goblin Patrol");
    }

    @Test
    @DisplayName("Paying echo keeps Goblin Patrol and echo does not trigger again")
    void payingEchoKeepsGoblinPatrolAndIsOneShot() {
        castAndResolveGoblinPatrol();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Goblin Patrol");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Goblin Patrol");
    }

    @Test
    @DisplayName("Echo creates no enters-the-battlefield trigger")
    void echoDoesNotTriggerOnEntry() {
        harness.castFromHand(player1, new GoblinPatrol(), "{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goblin Patrol");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's upkeep does not consume the pending echo obligation")
    void echoWaitsForControllersUpkeep() {
        castAndResolveGoblinPatrol();

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Goblin Patrol");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Goblin Patrol");
        harness.assertInGraveyard(player1, "Goblin Patrol");
    }

    private void castAndResolveGoblinPatrol() {
        harness.castFromHand(player1, new GoblinPatrol(), "{R}");
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Goblin Patrol");
    }
}
