package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TolarianSerpent;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DealDamageToPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SawtoothOgre.class, StripedBears.class, TolarianSerpent.class})
class SawtoothOgreTest extends BaseCardTest {

    @Test
    @DisplayName("When Sawtooth Ogre becomes blocked, the blocker is dealt 1 damage at end of combat")
    void becomesBlockedDamagesBlocker() {
        Permanent ogre = addCreatureReady(player1, new SawtoothOgre());
        ogre.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new StripedBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities(); // resolve the becomes-blocked trigger

        assertThat(gd.getDelayedActions(DealDamageToPermanentAtEndOfCombat.class))
                .anyMatch(a -> a.permanentId().equals(blocker.getId()) && a.damage() == 1);
        assertThat(blocker.getMarkedDamage()).isZero();

        leaveEndOfCombat();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Striped Bears");
    }

    @Test
    @DisplayName("When Sawtooth Ogre blocks, the attacker is dealt 1 damage at end of combat")
    void blocksDamagesAttacker() {
        Permanent attacker = addCreatureReady(player1, new StripedBears());
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
        Permanent firstBlocker = addCreatureReady(player2, new StripedBears());
        Permanent secondBlocker = addCreatureReady(player2, new StripedBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gd.getDelayedActions(DealDamageToPermanentAtEndOfCombat.class))
                .anyMatch(a -> a.permanentId().equals(firstBlocker.getId()))
                .anyMatch(a -> a.permanentId().equals(secondBlocker.getId()));

        leaveEndOfCombat();

        assertThat(firstBlocker.getMarkedDamage()).isEqualTo(1);
        assertThat(secondBlocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Nothing is scheduled when Sawtooth Ogre neither blocks nor is blocked")
    void noDamageOutsideCombat() {
        addCreatureReady(player1, new SawtoothOgre());
        Permanent blocker = addCreatureReady(player2, new StripedBears());

        leaveEndOfCombat();

        assertThat(gd.hasDelayedAction(DealDamageToPermanentAtEndOfCombat.class)).isFalse();
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The delayed damage still resolves if Sawtooth Ogre leaves during combat")
    void delayedDamageSurvivesSourceLeaving() {
        Permanent ogre = addCreatureReady(player1, new SawtoothOgre());
        ogre.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new TolarianSerpent());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities(); // resolve the becomes-blocked trigger

        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertInGraveyard(player1, "Sawtooth Ogre");
        assertThat(blocker.getMarkedDamage()).isEqualTo(4);
    }

    private void leaveEndOfCombat() {
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
    }
}
