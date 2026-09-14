package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CrenellatedWall;
import com.github.laxika.magicalvibes.cards.i.IronLance;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Warpath.class, CrenellatedWall.class, IronLance.class})
class WarpathTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to each blocked creature and each blocking creature")
    void damagesBlockedAndBlockingCreatures() {
        Permanent blocked = addCreatureReady(player1, new CrenellatedWall());
        blocked.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new CrenellatedWall());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(blocked.getId());

        Permanent unblocked = addCreatureReady(player1, new CrenellatedWall());
        unblocked.setAttacking(true);

        Permanent idle = addCreatureReady(player2, new CrenellatedWall());

        castWarpath();

        assertThat(blocked.getMarkedDamage()).isEqualTo(3);
        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
        assertThat(unblocked.getMarkedDamage()).isZero();
        assertThat(idle.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not damage noncreature permanents")
    void doesNotDamageNoncreaturePermanents() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new IronLance());
        noncreature.setBlocking(true);

        castWarpath();

        assertThat(noncreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Deals no damage when there are no blocked or blocking creatures")
    void doesNotDamageCreaturesOutsideCombat() {
        Permanent creature = addCreatureReady(player1, new CrenellatedWall());

        castWarpath();

        assertThat(creature.getMarkedDamage()).isZero();
    }

    private void castWarpath() {
        harness.castFromHand(player1, new Warpath(), "{3}{R}");
        harness.passBothPriorities();
    }
}
