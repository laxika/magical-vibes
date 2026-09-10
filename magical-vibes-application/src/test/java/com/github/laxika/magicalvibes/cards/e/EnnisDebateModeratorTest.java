package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnnisDebateModeratorTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles another creature until the next end step and gets a counter")
    void flickersCreatureAndGetsCounter() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castEnnis(List.of(creature.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        Permanent ennis = findPermanent(player1, "Ennis, Debate Moderator");
        assertThat(ennis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only another creature you control is a legal ETB target")
    void onlyAnotherControlledCreatureIsLegalTarget() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EnnisDebateModerator()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature you control");

        harness.castCreature(player1, 0, List.of(ownCreature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Does not get a counter when no card was exiled this turn")
    void noCounterWithoutExile() {
        harness.addToBattlefield(player1, new EnnisDebateModerator());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Ennis, Debate Moderator")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castEnnis(List<UUID> targetIds) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new EnnisDebateModerator()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0, targetIds);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
