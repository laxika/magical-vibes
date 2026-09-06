package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BeastWalkers;
import com.github.laxika.magicalvibes.cards.t.TimmerianFiends;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerraInquisitors.class, BeastWalkers.class, TimmerianFiends.class})
class SerraInquisitorsTest extends BaseCardTest {

    @Test
    @DisplayName("When Serra Inquisitors becomes blocked by a black creature it gets +2/+0")
    void becomesBlockedByBlackBoosts() {
        Permanent inquisitors = addCreatureReady(player1, new SerraInquisitors());
        inquisitors.setAttacking(true);
        addCreatureReady(player2, new TimmerianFiends());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(inquisitors.getPowerModifier()).isEqualTo(2);
        assertThat(inquisitors.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("When Serra Inquisitors becomes blocked by a nonblack creature it gets no boost")
    void becomesBlockedByNonblackDoesNothing() {
        Permanent inquisitors = addCreatureReady(player1, new SerraInquisitors());
        inquisitors.setAttacking(true);
        addCreatureReady(player2, new BeastWalkers());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(inquisitors.getPowerModifier()).isZero();
        assertThat(inquisitors.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("When Serra Inquisitors blocks a black creature it gets +2/+0")
    void blocksBlackBoosts() {
        Permanent attacker = addCreatureReady(player1, new TimmerianFiends());
        attacker.setAttacking(true);
        Permanent inquisitors = addCreatureReady(player2, new SerraInquisitors());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(inquisitors.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("When Serra Inquisitors blocks a nonblack creature it gets no boost")
    void blocksNonblackDoesNothing() {
        Permanent attacker = addCreatureReady(player1, new BeastWalkers());
        attacker.setAttacking(true);
        Permanent inquisitors = addCreatureReady(player2, new SerraInquisitors());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(inquisitors.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Blocked by two black creatures, Serra Inquisitors gets +2/+0 only once")
    void becomesBlockedByTwoBlackCreaturesBoostsOnce() {
        Permanent inquisitors = addCreatureReady(player1, new SerraInquisitors());
        inquisitors.setAttacking(true);
        addCreatureReady(player2, new TimmerianFiends());
        addCreatureReady(player2, new TimmerianFiends());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(inquisitors.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("When Serra Inquisitors blocks two black creatures it gets +2/+0 only once")
    void blocksTwoBlackCreaturesBoostsOnce() {
        Permanent firstAttacker = addCreatureReady(player1, new TimmerianFiends());
        firstAttacker.setAttacking(true);
        Permanent secondAttacker = addCreatureReady(player1, new TimmerianFiends());
        secondAttacker.setAttacking(true);
        SerraInquisitors card = new SerraInquisitors();
        card.addEffect(EffectSlot.STATIC, new CanBlockAnyNumberOfCreaturesEffect());
        Permanent inquisitors = addCreatureReady(player2, card);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)));
        harness.passUntil(TurnStep.COMBAT_DAMAGE);

        assertThat(inquisitors.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Serra Inquisitors' combat boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent inquisitors = addCreatureReady(player1, new SerraInquisitors());
        inquisitors.setAttacking(true);
        addCreatureReady(player2, new TimmerianFiends());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(inquisitors.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(inquisitors.getPowerModifier()).isZero();
    }
}
