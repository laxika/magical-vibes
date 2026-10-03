package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.h.HappyHoganDauntlessDriver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BobReluctantHYDRAAgent.class, HappyHoganDauntlessDriver.class})
class BobReluctantHYDRAAgentTest extends BaseCardTest {

    @Test
    void attackingAloneReturnsBobAndChangesLifeTotals() {
        Permanent bob = addCreatureReady(player1, new BobReluctantHYDRAAgent());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertInHand(player1, "Bob, Reluctant HYDRA Agent");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bob);
    }

    @Test
    void attackingWithAnotherCreatureDoesNotTrigger() {
        Permanent bob = addCreatureReady(player1, new BobReluctantHYDRAAgent());
        addCreatureReady(player1, new HappyHoganDauntlessDriver());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bob);
        harness.assertNotInHand(player1, "Bob, Reluctant HYDRA Agent");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
    }

    @Test
    void stolenBobReturnsToOwnerButLifeChangesApplyToTriggerController() {
        Permanent bob = addCreatureReady(player1, new BobReluctantHYDRAAgent());
        gd.stolenCreatures.put(bob.getId(), player2.getId());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertInHand(player2, "Bob, Reluctant HYDRA Agent");
        harness.assertNotInHand(player1, "Bob, Reluctant HYDRA Agent");
        harness.assertNotOnBattlefield(player1, "Bob, Reluctant HYDRA Agent");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void bobLeavingBeforeTriggerResolvesPreventsLifeChanges() {
        Permanent bob = addCreatureReady(player1, new BobReluctantHYDRAAgent());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(bob);
        gd.playerGraveyards.get(player1.getId()).add(bob.getCard());

        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Bob, Reluctant HYDRA Agent");
        harness.assertNotInHand(player1, "Bob, Reluctant HYDRA Agent");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void bobReturningToBattlefieldIsNotBouncedByHisOldTrigger() {
        Permanent bob = addCreatureReady(player1, new BobReluctantHYDRAAgent());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(bob);
        Permanent returnedBob = addCreatureReady(player1, bob.getCard());

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(returnedBob);
        harness.assertNotInHand(player1, "Bob, Reluctant HYDRA Agent");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
