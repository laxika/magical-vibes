package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BrassSecretary;
import com.github.laxika.magicalvibes.cards.e.ElvishLookout;
import com.github.laxika.magicalvibes.cards.p.PhyrexianMonitor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SquirmingMass.class, ElvishLookout.class, PhyrexianMonitor.class, BrassSecretary.class})
class SquirmingMassTest extends BaseCardTest {

    private Permanent prepareBlocker(Card blockerCard) {
        Permanent blocker = addCreatureReady(player2, blockerCard);
        addCreatureReady(player1, new SquirmingMass());
        declareAttackersAndPrepareBlockers(List.of(0));
        return blocker;
    }

    @Test
    @DisplayName("Fear prevents a nonblack, nonartifact creature from blocking")
    void nonblackNonartifactCreatureCannotBlock() {
        prepareBlocker(new ElvishLookout());

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fear");
    }

    @Test
    @DisplayName("Fear allows a black creature to block")
    void blackCreatureCanBlock() {
        Permanent blocker = prepareBlocker(new PhyrexianMonitor());

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Fear allows an artifact creature to block")
    void artifactCreatureCanBlock() {
        Permanent blocker = prepareBlocker(new BrassSecretary());

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Fear does not allow a tapped artifact creature to block")
    void tappedArtifactCreatureCannotBlock() {
        Permanent blocker = prepareBlocker(new BrassSecretary());
        blocker.tap();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);

        assertThat(blocker.isBlocking()).isFalse();
    }
}
