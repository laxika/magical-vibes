package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Brushstrider;
import com.github.laxika.magicalvibes.cards.t.ToweringIndrik;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkylinePredator.class, Brushstrider.class, ToweringIndrik.class})
class SkylinePredatorTest extends BaseCardTest {

    @Test
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new SkylinePredator(), "{4}{U}{U}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Skyline Predator");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canFlashInAfterAttackersAreDeclaredAndBlockImmediately() {
        addCreatureReady(player1, new Brushstrider());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        harness.castFromHand(player2, new SkylinePredator(), "{4}{U}{U}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Skyline Predator");

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Brushstrider");
        harness.assertOnBattlefield(player2, "Skyline Predator");
        harness.assertLife(player2, 20);
    }

    @Test
    void groundCreatureCannotBlockFlyingAttacker() {
        addCreatureReady(player1, new SkylinePredator());
        harness.addToBattlefield(player2, new Brushstrider());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void flyingCreatureCanBlockAndBothSurviveCombat() {
        addCreatureReady(player1, new SkylinePredator());
        harness.addToBattlefield(player2, new SkylinePredator());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Skyline Predator");
        harness.assertOnBattlefield(player2, "Skyline Predator");
        harness.assertLife(player2, 20);
    }

    @Test
    void reachCreatureCanBlockFlyingAttacker() {
        addCreatureReady(player1, new SkylinePredator());
        harness.addToBattlefield(player2, new ToweringIndrik());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Skyline Predator");
        harness.assertOnBattlefield(player2, "Towering Indrik");
        harness.assertLife(player2, 20);
    }
}
