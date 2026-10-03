package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DutifulServants;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvenReedstalker.class, DutifulServants.class})
class AvenReedstalkerTest extends BaseCardTest {

    @Test
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new AvenReedstalker(), "{3}{U}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Aven Reedstalker");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canCastDuringCombat() {
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new AvenReedstalker(), "{3}{U}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Aven Reedstalker");
    }

    @Test
    void nonflyingCreatureCannotBlock() {
        addCreatureReady(player1, new AvenReedstalker());
        addCreatureReady(player2, new DutifulServants());
        declareAttackersAndPrepareBlockers(List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    void canBlockAnotherFlyingCreature() {
        addCreatureReady(player1, new AvenReedstalker());
        Permanent blocker = addCreatureReady(player2, new AvenReedstalker());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBlockNonflyingCreature() {
        addCreatureReady(player1, new DutifulServants());
        Permanent blocker = addCreatureReady(player2, new AvenReedstalker());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }
}
