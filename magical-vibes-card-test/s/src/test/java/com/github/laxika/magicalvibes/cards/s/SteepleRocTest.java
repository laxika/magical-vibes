package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MurmuringPhantasm;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SteepleRoc.class, MurmuringPhantasm.class, WindDrake.class})
class SteepleRocTest extends BaseCardTest {

    @Test
    void groundCreatureCannotBlock() {
        addCreatureReady(player1, new SteepleRoc());
        addCreatureReady(player2, new MurmuringPhantasm());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void firstStrikeKillsFlyingBlockerBeforeItDealsDamage() {
        addCreatureReady(player1, new SteepleRoc());
        addCreatureReady(player2, new WindDrake());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Steeple Roc");
        harness.assertInGraveyard(player2, "Wind Drake");
        harness.assertNotOnBattlefield(player2, "Wind Drake");
        harness.assertLife(player2, 20);
    }

    @Test
    void firstStrikeAlsoKillsAnAttackerBeforeItDealsDamage() {
        addCreatureReady(player1, new WindDrake());
        addCreatureReady(player2, new SteepleRoc());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Wind Drake");
        harness.assertNotOnBattlefield(player1, "Wind Drake");
        harness.assertOnBattlefield(player2, "Steeple Roc");
        harness.assertLife(player2, 20);
    }

    @Test
    void unblockedFirstStrikerDealsDamageOnlyOnce() {
        addCreatureReady(player1, new SteepleRoc());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player1, "Steeple Roc");
    }
}
