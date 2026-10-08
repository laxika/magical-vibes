package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.s.SaguArcher;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({VenerableLammasu.class, AlpineGrizzly.class, SaguArcher.class})
class VenerableLammasuTest extends BaseCardTest {

    @Test
    @DisplayName("Venerable Lammasu cannot be blocked by a creature without flying or reach")
    void cannotBeBlockedByNormalCreature() {
        Permanent lammasu = addCreatureReady(player1, new VenerableLammasu());
        lammasu.setAttacking(true);
        addCreatureReady(player2, new AlpineGrizzly());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Venerable Lammasu can be blocked by a creature with flying")
    void canBeBlockedByFlyingCreature() {
        Permanent lammasu = addCreatureReady(player1, new VenerableLammasu());
        lammasu.setAttacking(true);
        addCreatureReady(player2, new VenerableLammasu());
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Venerable Lammasu can be blocked by a creature with reach")
    void canBeBlockedByReachCreature() {
        Permanent lammasu = addCreatureReady(player1, new VenerableLammasu());
        lammasu.setAttacking(true);
        addCreatureReady(player2, new SaguArcher());
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Venerable Lammasu can block a creature without flying")
    void canBlockGroundCreature() {
        Permanent grizzly = addCreatureReady(player1, new AlpineGrizzly());
        grizzly.setAttacking(true);
        addCreatureReady(player2, new VenerableLammasu());
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
        harness.assertLife(player2, 20);
    }
}
