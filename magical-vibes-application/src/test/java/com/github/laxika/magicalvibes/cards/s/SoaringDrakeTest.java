package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AutomaticLibrarian;
import com.github.laxika.magicalvibes.cards.m.MagnigothSentry;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoaringDrake.class, AutomaticLibrarian.class, MagnigothSentry.class})
class SoaringDrakeTest extends BaseCardTest {

    @Test
    void flyingPreventsNonflyingCreatureFromBlocking() {
        addCreatureReady(player1, new SoaringDrake());
        addCreatureReady(player2, new AutomaticLibrarian());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flyingCreatureCanBlockSoaringDrake() {
        addCreatureReady(player1, new SoaringDrake());
        addCreatureReady(player2, new SoaringDrake());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Soaring Drake");
        harness.assertOnBattlefield(player2, "Soaring Drake");
    }

    @Test
    void reachCreatureCanBlockSoaringDrake() {
        addCreatureReady(player1, new SoaringDrake());
        addCreatureReady(player2, new MagnigothSentry());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Soaring Drake");
        harness.assertOnBattlefield(player2, "Magnigoth Sentry");
    }

    @Test
    void soaringDrakeCanBlockNonflyingCreature() {
        addCreatureReady(player1, new AutomaticLibrarian());
        addCreatureReady(player2, new SoaringDrake());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Automatic Librarian");
        harness.assertInGraveyard(player2, "Soaring Drake");
    }
}
