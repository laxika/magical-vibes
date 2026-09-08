package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WarpathTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to each blocked creature and each blocking creature")
    void damagesBlockedAndBlockingCreatures() {
        Permanent blocked = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        blocked.setSummoningSick(false);
        blocked.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(blocked.getId());

        Permanent unblocked = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        unblocked.setSummoningSick(false);
        unblocked.setAttacking(true);

        Permanent idle = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        idle.setSummoningSick(false);

        castWarpath();

        assertThat(blocked.getMarkedDamage()).isEqualTo(3);
        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
        assertThat(unblocked.getMarkedDamage()).isZero();
        assertThat(idle.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Deals no damage when there are no blocked or blocking creatures")
    void doesNotDamageCreaturesOutsideCombat() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        creature.setSummoningSick(false);

        castWarpath();

        assertThat(creature.getMarkedDamage()).isZero();
    }

    private void castWarpath() {
        harness.setHand(player1, List.of(new Warpath()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
