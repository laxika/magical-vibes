package com.github.laxika.magicalvibes.cards.r;

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

class RashkaTheSlayerTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking a black creature gives Rashka +1/+2")
    void blocksBlackCreatureBoosts() {
        addReady(player1, new VampireAristocrat()).setAttacking(true);
        Permanent rashka = addReady(player2, new RashkaTheSlayer());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(rashka.getPowerModifier()).isEqualTo(1);
        assertThat(rashka.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Blocking a nonblack creature gives Rashka nothing")
    void blocksNonBlackCreatureDoesNothing() {
        addReady(player1, new GrizzlyBears()).setAttacking(true);
        Permanent rashka = addReady(player2, new RashkaTheSlayer());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(rashka.getPowerModifier()).isZero();
        assertThat(rashka.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Being blocked by a black creature does not trigger Rashka")
    void becomesBlockedDoesNothing() {
        Permanent rashka = addReady(player1, new RashkaTheSlayer());
        rashka.setAttacking(true);
        addReady(player2, new VampireAristocrat());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(rashka.getPowerModifier()).isZero();
        assertThat(rashka.getToughnessModifier()).isZero();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
