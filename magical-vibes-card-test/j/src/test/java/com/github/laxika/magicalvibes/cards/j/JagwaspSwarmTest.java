package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrapplerSpider;
import com.github.laxika.magicalvibes.cards.l.LeatherbackBaloth;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({JagwaspSwarm.class, LeatherbackBaloth.class, GrapplerSpider.class})
class JagwaspSwarmTest extends BaseCardTest {

    @Test
    void flyingPreventsGroundCreatureFromBlocking() {
        addCreatureReady(player1, new JagwaspSwarm()).setAttacking(true);
        addCreatureReady(player2, new LeatherbackBaloth());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flyingCreatureCanBlockSwarm() {
        addCreatureReady(player1, new JagwaspSwarm()).setAttacking(true);
        addCreatureReady(player2, new JagwaspSwarm());

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    void reachCreatureCanBlockSwarm() {
        addCreatureReady(player1, new JagwaspSwarm()).setAttacking(true);
        addCreatureReady(player2, new GrapplerSpider());

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    void swarmCanBlockGroundCreature() {
        addCreatureReady(player1, new LeatherbackBaloth()).setAttacking(true);
        addCreatureReady(player2, new JagwaspSwarm());

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }
}
