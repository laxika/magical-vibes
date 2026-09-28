package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.j.JuganTheRisingStar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathcurseOgre.class, JuganTheRisingStar.class})
class DeathcurseOgreTest extends BaseCardTest {

    @Test
    @DisplayName("Dying puts the death trigger on the stack")
    void deathTriggerGoesOnStack() {
        harness.addToBattlefield(player1, new DeathcurseOgre());

        makeOgreDieInCombat();

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

        makeOgreDieInCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Each Deathcurse Ogre that dies creates its own life-loss trigger")
    void eachDyingOgreCreatesItsOwnTrigger() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent firstOgre = addCreatureReady(player1, new DeathcurseOgre());
        Permanent secondOgre = addCreatureReady(player1, new DeathcurseOgre());

        firstOgre.setMarkedDamage(3);
        secondOgre.setMarkedDamage(3);
        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    /**
     * Deathcurse Ogre (3/3) attacks and is blocked by a 5/5, so it dies to combat damage.
     */
    private void makeOgreDieInCombat() {
        Permanent ogre = findPermanent(player1, "Deathcurse Ogre");
        ogre.setSummoningSick(false);
        ogre.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new JuganTheRisingStar());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
    }
}
