package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlabornZealot.class, AlabornTrooper.class})
class AlabornZealotTest extends BaseCardTest {

    private Permanent addZealotBlocker() {
        return addCreatureReady(player2, new AlabornZealot());
    }

    private Permanent addAttacker(int power, int toughness) {
        AlabornTrooper creature = new AlabornTrooper();
        creature.setPower(power);
        creature.setToughness(toughness);
        Permanent atkPerm = addCreatureReady(player1, creature);
        atkPerm.setAttacking(true);
        return atkPerm;
    }

    private void declareZealotBlock() {
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("Declaring Alaborn Zealot as blocker pushes a triggered ability onto the stack")
    void blockTriggerPushesOntoStack() {
        Permanent zealotPerm = addZealotBlocker();
        Permanent atkPerm = addAttacker(2, 2);

        declareZealotBlock();

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.isNonTargeting()).isTrue();
        assertThat(entry.getTargetId()).isEqualTo(atkPerm.getId());
        assertThat(entry.getSourcePermanentId()).isEqualTo(zealotPerm.getId());
    }

    @Test
    @DisplayName("When the block trigger resolves, both creatures are destroyed")
    void blockTriggerDestroysBothCreatures() {
        addZealotBlocker();
        addAttacker(10, 10);

        declareZealotBlock();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Alaborn Trooper");
        harness.assertInGraveyard(player2, "Alaborn Zealot");
        harness.assertNotOnBattlefield(player1, "Alaborn Trooper");
        harness.assertNotOnBattlefield(player2, "Alaborn Zealot");
    }

    @Test
    @DisplayName("Blocked attacker destroyed by the trigger deals no combat damage to the player")
    void destroyedAttackerDealsNoDamageToPlayer() {
        harness.setLife(player2, 20);
        addZealotBlocker();
        addAttacker(5, 5);

        declareZealotBlock();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The trigger still destroys the attacker if Alaborn Zealot leaves before resolution")
    void triggerDestroysAttackerIfZealotLeavesBeforeResolution() {
        Permanent zealotPerm = addZealotBlocker();
        addAttacker(2, 2);

        declareZealotBlock();
        gd.playerBattlefields.get(player2.getId()).remove(zealotPerm);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Alaborn Trooper");
        harness.assertNotOnBattlefield(player1, "Alaborn Trooper");
    }

    @Test
    @DisplayName("The trigger still destroys Alaborn Zealot if the attacker leaves before resolution")
    void triggerDestroysZealotIfAttackerLeavesBeforeResolution() {
        addZealotBlocker();
        Permanent attacker = addAttacker(2, 2);

        declareZealotBlock();
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Alaborn Zealot");
        harness.assertNotOnBattlefield(player2, "Alaborn Zealot");
    }

    @Test
    @DisplayName("A normal creature blocking does not push any trigger onto the stack")
    void normalCreatureDoesNotTriggerOnBlock() {
        addCreatureReady(player2, new AlabornTrooper());
        addAttacker(2, 2);

        declareZealotBlock();

        assertThat(gd.stack).isEmpty();
    }
}
