package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeathcurseOgreTest extends BaseCardTest {

    @Test
    @DisplayName("Dying puts the death trigger on the stack")
    void deathTriggerGoesOnStack() {
        harness.addToBattlefield(player1, new DeathcurseOgre());

        setupCombatWhereOgreDies();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Deathcurse Ogre");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Deathcurse Ogre");
    }

    @Test
    @DisplayName("Resolving the death trigger makes each player lose 3 life, including the controller")
    void eachPlayerLosesThreeLife() {
        harness.addToBattlefield(player1, new DeathcurseOgre());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupCombatWhereOgreDies();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    /**
     * Deathcurse Ogre (3/3) attacks and is blocked by a 5/5, so it dies to combat damage.
     */
    private void setupCombatWhereOgreDies() {
        Permanent ogre = findPermanent(player1, "Deathcurse Ogre");
        ogre.setSummoningSick(false);
        ogre.setAttacking(true);

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(5);
        bigBear.setToughness(5);
        Permanent blocker = new Permanent(bigBear);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
