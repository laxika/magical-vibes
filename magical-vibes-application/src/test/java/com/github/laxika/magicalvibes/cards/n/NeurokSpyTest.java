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

        addCreatureReady(player2, new FangrenHunter());

        addCreatureReady(player1, new NeurokSpy());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Neurok Spy can be blocked when defending player controls no artifacts")
    void canBeBlockedWhenDefenderControlsNoArtifact() {
        Permanent blocker = addCreatureReady(player2, new FangrenHunter());

        addCreatureReady(player1, new NeurokSpy());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Neurok Spy can be blocked when only the attacking player controls an artifact")
    void attackingPlayersArtifactDoesNotCount() {
        harness.addToBattlefield(player1, new Ornithopter());
        addCreatureReady(player1, new NeurokSpy());

        Permanent blocker = addCreatureReady(player2, new FangrenHunter());

        declareAttackersAndPrepareBlockers(List.of(1));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Neurok Spy can't be blocked when defending player controls a noncreature artifact")
    void noncreatureArtifactStillCounts() {
        harness.addToBattlefield(player2, new Bonesplitter());
        addCreatureReady(player2, new FangrenHunter());
        addCreatureReady(player1, new NeurokSpy());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("A tapped artifact still makes Neurok Spy unblockable")
    void tappedArtifactStillCounts() {
        harness.addToBattlefield(player2, new Bonesplitter());
        findPermanent(player2, "Bonesplitter").tap();
        addCreatureReady(player2, new FangrenHunter());
        addCreatureReady(player1, new NeurokSpy());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Neurok Spy checks player one's artifacts when player two attacks")
    void checksDefendingPlayerWhenPlayerTwoAttacks() {
        harness.addToBattlefield(player1, new Bonesplitter());
        addCreatureReady(player1, new FangrenHunter());
        addCreatureReady(player2, new NeurokSpy());
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }
}
