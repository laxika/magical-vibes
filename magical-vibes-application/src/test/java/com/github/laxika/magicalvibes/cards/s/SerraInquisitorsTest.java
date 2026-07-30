package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VampireAristocrat;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SerraInquisitorsTest extends BaseCardTest {

    @Test
    @DisplayName("When Serra Inquisitors becomes blocked by a black creature it gets +2/+0")
    void becomesBlockedByBlackBoosts() {
        Permanent inquisitors = addReady(player1, new SerraInquisitors());
        inquisitors.setAttacking(true);
        addReady(player2, new VampireAristocrat());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(inquisitors.getPowerModifier()).isEqualTo(2);
        assertThat(inquisitors.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("When Serra Inquisitors becomes blocked by a nonblack creature it gets no boost")
    void becomesBlockedByNonblackDoesNothing() {
        Permanent inquisitors = addReady(player1, new SerraInquisitors());
        inquisitors.setAttacking(true);
        addReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(inquisitors.getPowerModifier()).isZero();
        assertThat(inquisitors.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("When Serra Inquisitors blocks a black creature it gets +2/+0")
    void blocksBlackBoosts() {
        Permanent attacker = addReady(player1, new VampireAristocrat());
        attacker.setAttacking(true);
        Permanent inquisitors = addReady(player2, new SerraInquisitors());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(inquisitors.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("When Serra Inquisitors blocks a nonblack creature it gets no boost")
    void blocksNonblackDoesNothing() {
        Permanent attacker = addReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent inquisitors = addReady(player2, new SerraInquisitors());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(inquisitors.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Blocked by two black creatures, Serra Inquisitors gets +2/+0 only once")
    void becomesBlockedByTwoBlackCreaturesBoostsOnce() {
        Permanent inquisitors = addReady(player1, new SerraInquisitors());
        inquisitors.setAttacking(true);
        addReady(player2, new VampireAristocrat());
        addReady(player2, new VampireAristocrat());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(inquisitors.getPowerModifier()).isEqualTo(2);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
