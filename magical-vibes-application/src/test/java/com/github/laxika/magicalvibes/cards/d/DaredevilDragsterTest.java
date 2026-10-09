package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AetherstreamLeopard;
import com.github.laxika.magicalvibes.cards.l.LeaveInTheDust;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaredevilDragster.class, AetherstreamLeopard.class, LeaveInTheDust.class})
class DaredevilDragsterTest extends BaseCardTest {

    @Test
    void crewAnimatesDragsterAndTapsCrew() {
        Permanent dragster = addDragsterReady(player1);
        Permanent crew = addCreatureReady(player1, new AetherstreamLeopard());

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

    @Test
    void blockedDragsterGetsCounterDuringOpponentsCombat() {
        Permanent dragster = addDragsterReady(player2);
        dragster.setBlocking(true);

        leaveEndOfCombat();

        assertThat(dragster.getCounterCount(CounterType.VELOCITY)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(dragster);
    }

    @Test
    void leavingCombatDoesNotEraseHavingAttacked() {
        Permanent dragster = addDragsterReady(player1);
        dragster.setAttacking(true);
        dragster.setAttacking(false);

        leaveEndOfCombat();

        assertThat(dragster.getCounterCount(CounterType.VELOCITY)).isEqualTo(1);
    }

    @Test
    void twoExistingCountersDoNotCauseSacrificeWithoutCombatParticipation() {
        Permanent dragster = addDragsterReady(player1);
        dragster.setCounterCount(CounterType.VELOCITY, 2);
        int handSize = gd.playerHands.get(player1.getId()).size();

        leaveEndOfCombat();

        assertThat(dragster.getCounterCount(CounterType.VELOCITY)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dragster);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    void moreThanTwoCountersAlsoSacrificesAndDraws() {
        Permanent dragster = addDragsterReady(player1);
        dragster.setCounterCount(CounterType.VELOCITY, 3);
        dragster.setAttacking(true);
        int handSize = gd.playerHands.get(player1.getId()).size();

        leaveEndOfCombat();

        harness.assertInGraveyard(player1, "Daredevil Dragster");
        harness.assertNotOnBattlefield(player1, "Daredevil Dragster");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 2);
    }

    @Test
    void bouncedWithOneCounterDoesNotDraw() {
        assertDrawAfterBounce(1, 0);
    }

    @Test
    void bouncedWithTwoCountersStillDraws() {
        assertDrawAfterBounce(2, 2);
    }

    @Test
    void summoningSickCreatureCanCrew() {
        Permanent dragster = addDragsterReady(player1);
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new AetherstreamLeopard());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, dragster)).isTrue();
    }

    @Test
    void crewAnimationEndsAtEndOfTurn() {
        Permanent dragster = addDragsterReady(player1);
        addCreatureReady(player1, new AetherstreamLeopard());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, dragster)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, dragster)).isFalse();
    }

    private void assertDrawAfterBounce(int counters, int cardsDrawn) {
        Permanent dragster = addDragsterReady(player1);
        dragster.setCounterCount(CounterType.VELOCITY, counters);
        dragster.setAttacking(true);
        int deckSize = gd.playerDecks.get(player1.getId()).size();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of(new LeaveInTheDust()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player2, 0, dragster.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Daredevil Dragster");
        harness.assertInHand(player1, "Daredevil Dragster");
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSize - cardsDrawn);
    }

    private Permanent addDragsterReady(Player player) {
        return addCreatureReady(player, new DaredevilDragster());
    }

    private void leaveEndOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_OF_COMBAT);
        resolveAllTriggers();
    }
}
