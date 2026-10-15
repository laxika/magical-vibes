package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MausoleumWanderer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwiftSpinner.class, MausoleumWanderer.class})
class SwiftSpinnerTest extends BaseCardTest {

    @Test
    void castsDuringOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.castFromHand(player1, new SwiftSpinner(), "{3}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Swift Spinner");
        harness.assertNotInHand(player1, "Swift Spinner");
    }

    @Test
    void summoningSickSpinnerCanBlockFlyingCreature() {
        addCreatureReady(player1, new MausoleumWanderer());
        Permanent spinner = harness.addToBattlefieldAndReturn(player2, new SwiftSpinner());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spinner.isBlocking()).isTrue();
    }

    @Test
    void reachDoesNotRestrictBlockingNonFlyingCreatures() {
        addCreatureReady(player1, new SwiftSpinner());
        Permanent spinner = harness.addToBattlefieldAndReturn(player2, new SwiftSpinner());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spinner.isBlocking()).isTrue();
    }

    @Test
    void tappedSpinnerCannotBlockFlyingCreature() {
        addCreatureReady(player1, new MausoleumWanderer());
        Permanent spinner = harness.addToBattlefieldAndReturn(player2, new SwiftSpinner());

        declareAttackersAndPrepareBlockers(List.of(0));
        spinner.tap();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
        assertThat(spinner.isBlocking()).isFalse();
    }
}
