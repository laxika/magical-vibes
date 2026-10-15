package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.t.TajuruPathwarden;
import com.github.laxika.magicalvibes.cards.w.Wastes;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReaverDrone.class, TajuruPathwarden.class, Wastes.class})
class ReaverDroneTest extends BaseCardTest {

    @Test
    void losesLifeWithoutAnotherColorlessCreature() {
        addDrone(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void doesNotLoseLifeWithAnotherColorlessCreature() {
        addDrone(player1);
        addDrone(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void anotherCreatureThatIsNotColorlessDoesNotPreventLifeLoss() {
        addDrone(player1);
        addCreature(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void doesNotLoseLifeDuringOpponentsUpkeep() {
        addDrone(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void checksUnlessConditionWhenAbilityResolves() {
        addDrone(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        addDrone(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    private void addDrone(Player player) {
        harness.addToBattlefield(player, new ReaverDrone());
    }

    private void addCreature(Player player) {
        harness.addToBattlefield(player, new TajuruPathwarden());
    }

    @Test
    void opponentsColorlessCreatureDoesNotPreventLifeLoss() {
        addDrone(player1);
        addDrone(player2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore - 1);
        harness.assertLife(player2, opponentLifeBefore);
    }

    @Test
    void colorlessNoncreatureDoesNotPreventLifeLoss() {
        addDrone(player1);
        harness.addToBattlefield(player1, new Wastes());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    void bothAbilitiesStillResolveWhenAnotherDroneLeavesAfterTriggering() {
        addDrone(player1);
        Permanent otherDrone = harness.addToBattlefieldAndReturn(player1, new ReaverDrone());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);
        gd.playerBattlefields.get(player1.getId()).remove(otherDrone);
        gd.playerGraveyards.get(player1.getId()).add(otherDrone.getCard());
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore - 1);
    }
}
