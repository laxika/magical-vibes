package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DealDamageToPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SawtoothOgreTest extends BaseCardTest {

    @Test
    @DisplayName("When Sawtooth Ogre becomes blocked, the blocker is dealt 1 damage at end of combat")
    void becomesBlockedDamagesBlocker() {
        Permanent ogre = addCreatureReady(player1, new SawtoothOgre());
        ogre.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GiantSpider()); // 2/4

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities(); // resolve the becomes-blocked trigger

        assertThat(gd.getDelayedActions(DealDamageToPermanentAtEndOfCombat.class))
                .anyMatch(a -> a.permanentId().equals(spider.getId()) && a.damage() == 1);
        assertThat(spider.getMarkedDamage()).isZero();

        leaveEndOfCombat();

        assertThat(spider.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("When Sawtooth Ogre blocks, the attacker is dealt 1 damage at end of combat")
    void blocksDamagesAttacker() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider()); // 2/4
        attacker.setAttacking(true);
        addCreatureReady(player2, new SawtoothOgre());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities(); // resolve the block trigger

        assertThat(gd.getDelayedActions(DealDamageToPermanentAtEndOfCombat.class))
                .anyMatch(a -> a.permanentId().equals(attacker.getId()) && a.damage() == 1);

        leaveEndOfCombat();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Each blocker is dealt 1 damage when Sawtooth Ogre is blocked by two creatures")
    void damagesEachBlocker() {
        Permanent ogre = addCreatureReady(player1, new SawtoothOgre());
        ogre.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GiantSpider()); // 2/4
        Permanent bears = addCreatureReady(player2, new GrizzlyBears()); // 2/2

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities(); // one trigger per blocker

        assertThat(gd.getDelayedActions(DealDamageToPermanentAtEndOfCombat.class))
                .anyMatch(a -> a.permanentId().equals(spider.getId()))
                .anyMatch(a -> a.permanentId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Nothing is scheduled when Sawtooth Ogre neither blocks nor is blocked")
    void noDamageOutsideCombat() {
        addCreatureReady(player1, new SawtoothOgre());
        Permanent spider = addCreatureReady(player2, new GiantSpider());

        leaveEndOfCombat();

        assertThat(gd.hasDelayedAction(DealDamageToPermanentAtEndOfCombat.class)).isFalse();
        assertThat(spider.getMarkedDamage()).isZero();
    }

    private void leaveEndOfCombat() {
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
