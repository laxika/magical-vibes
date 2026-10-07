package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PhyrexianHulk;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpireMonitor.class, PhyrexianHulk.class})
class SpireMonitorTest extends BaseCardTest {

    @Test
    void canBeCastDuringOpponentsUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castFromHand(player1, new SpireMonitor(), "{4}{U}");

        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Spire Monitor");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Spire Monitor");
        harness.assertNotInHand(player1, "Spire Monitor");
    }

    @Test
    void canRespondToAnotherCreatureSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new PhyrexianHulk(), "{6}");

        harness.castFromHand(player1, new SpireMonitor(), "{4}{U}");

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Spire Monitor");
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Spire Monitor");
        harness.assertOnBattlefield(player2, "Phyrexian Hulk");
    }

    @Test
    void groundCreatureCannotBlock() {
        addCreatureReady(player1, new SpireMonitor());
        addCreatureReady(player2, new PhyrexianHulk());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void canBlockAnotherFlyingCreature() {
        addCreatureReady(player1, new SpireMonitor());
        addCreatureReady(player2, new SpireMonitor());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Spire Monitor");
        harness.assertInGraveyard(player2, "Spire Monitor");
        harness.assertLife(player2, 20);
    }
}


