package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DreamFighterTest extends BaseCardTest {

    @Test
    @DisplayName("When Dream Fighter blocks a creature, both it and the attacker phase out")
    void blocksPhasesOutBoth() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent fighter = addCreatureReady(player2, new DreamFighter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(fighter);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(attacker);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(fighter);
    }

    @Test
    @DisplayName("When Dream Fighter becomes blocked, both it and the blocker phase out")
    void becomesBlockedPhasesOutBoth() {
        Permanent fighter = addCreatureReady(player1, new DreamFighter());
        fighter.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.findPermanentById(gd, fighter.getId())).isNull();
        assertThat(gqs.findPermanentById(gd, blocker.getId())).isNull();
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(fighter);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Phasing out removes both creatures from combat, so no combat damage is dealt")
    void phasedOutCreaturesDealNoCombatDamage() {
        Permanent fighter = addCreatureReady(player1, new DreamFighter());
        fighter.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        int startingLife = gd.getLife(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(fighter.isAttacking()).isFalse();
        assertThat(blocker.isBlocking()).isFalse();

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(fighter.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Each phased-out creature phases back in during its own controller's next untap step")
    void phasesBackInOnControllersNextUntapStep() {
        Permanent fighter = addCreatureReady(player1, new DreamFighter());
        fighter.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        advanceTurn(); // player2's untap step — their blocker phases in
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fighter);

        advanceTurn(); // player1's untap step — Dream Fighter phases in
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fighter);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }
}
