package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AlabornZealotTest extends BaseCardTest {

    private Permanent addZealotBlocker() {
        Permanent zealotPerm = new Permanent(new AlabornZealot());
        zealotPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(zealotPerm);
        return zealotPerm;
    }

    private Permanent addAttacker(int power, int toughness) {
        GrizzlyBears creature = new GrizzlyBears();
        creature.setPower(power);
        creature.setToughness(toughness);
        Permanent atkPerm = new Permanent(creature);
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);
        return atkPerm;
    }

    private void declareZealotBlock() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
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

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Alaborn Zealot"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Alaborn Zealot"));
    }

    @Test
    @DisplayName("Blocked attacker destroyed by the trigger deals no combat damage to the player")
    void destroyedAttackerDealsNoDamageToPlayer() {
        harness.setLife(player2, 20);
        addZealotBlocker();
        addAttacker(5, 5);

        declareZealotBlock();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A normal creature blocking does not push any trigger onto the stack")
    void normalCreatureDoesNotTriggerOnBlock() {
        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);
        addAttacker(2, 2);

        declareZealotBlock();

        assertThat(gd.stack).isEmpty();
    }
}
