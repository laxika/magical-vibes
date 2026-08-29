package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DaredevilDragsterTest extends BaseCardTest {

    @Test
    void crewAnimatesDragsterAndTapsCrew() {
        Permanent dragster = addDragsterReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, dragster)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void attackedDragsterGetsVelocityCounterAtEndOfCombat() {
        Permanent dragster = addDragsterReady(player1);
        dragster.setAttacking(true);
        int handSize = gd.playerHands.get(player1.getId()).size();

        leaveEndOfCombat();

        assertThat(dragster.getCounterCount(CounterType.VELOCITY)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dragster);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    void secondVelocityCounterSacrificesDragsterAndDrawsTwoCards() {
        Permanent dragster = addDragsterReady(player1);
        dragster.setCounterCount(CounterType.VELOCITY, 1);
        dragster.setAttacking(true);
        int handSize = gd.playerHands.get(player1.getId()).size();

        leaveEndOfCombat();

        harness.assertNotOnBattlefield(player1, "Daredevil Dragster");
        harness.assertInGraveyard(player1, "Daredevil Dragster");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 2);
    }

    @Test
    void dragsterThatDidNotAttackOrBlockGetsNoVelocityCounter() {
        Permanent dragster = addDragsterReady(player1);

        leaveEndOfCombat();

        assertThat(dragster.getCounterCount(CounterType.VELOCITY)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dragster);
    }

    private Permanent addDragsterReady(Player player) {
        Permanent permanent = new Permanent(new DaredevilDragster());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void leaveEndOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
