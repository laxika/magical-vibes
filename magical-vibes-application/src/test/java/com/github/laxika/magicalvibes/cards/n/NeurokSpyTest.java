package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.f.FangrenHunter;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NeurokSpy.class, Ornithopter.class, Bonesplitter.class, FangrenHunter.class})
class NeurokSpyTest extends BaseCardTest {

    @Test
    @DisplayName("Neurok Spy can't be blocked when defending player controls an artifact")
    void cantBeBlockedWhenDefenderControlsArtifact() {
        harness.addToBattlefield(player2, new Ornithopter());

        Permanent blocker = addCreatureReady(player2, new FangrenHunter());

        Permanent spy = addCreatureReady(player1, new NeurokSpy());
        spy.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Neurok Spy can be blocked when defending player controls no artifacts")
    void canBeBlockedWhenDefenderControlsNoArtifact() {
        Permanent blocker = addCreatureReady(player2, new FangrenHunter());

        Permanent spy = addCreatureReady(player1, new NeurokSpy());
        spy.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Neurok Spy can be blocked when only the attacking player controls an artifact")
    void attackingPlayersArtifactDoesNotCount() {
        harness.addToBattlefield(player1, new Ornithopter());
        Permanent spy = addCreatureReady(player1, new NeurokSpy());
        spy.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new FangrenHunter());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Neurok Spy can't be blocked when defending player controls a noncreature artifact")
    void noncreatureArtifactStillCounts() {
        harness.addToBattlefield(player2, new Bonesplitter());
        Permanent blocker = addCreatureReady(player2, new FangrenHunter());
        Permanent spy = addCreatureReady(player1, new NeurokSpy());
        spy.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }
}
