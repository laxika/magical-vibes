package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SentinelOfTheEternalWatchTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent findPermanent(Player owner, UUID id) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getId().equals(id))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Taps a creature the active opponent controls at the beginning of their combat")
    void tapsTargetOnOpponentsTurn() {
        harness.addToBattlefield(player1, new SentinelOfTheEternalWatch());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        advanceToCombat(player2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, bearsId).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger on its controller's own turn")
    void doesNotTriggerOnOwnTurn() {
        harness.addToBattlefield(player1, new SentinelOfTheEternalWatch());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player2, bearsId).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Only the active opponent's creatures are legal targets")
    void doesNotTriggerWithoutOpponentCreature() {
        harness.addToBattlefield(player1, new SentinelOfTheEternalWatch());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        advanceToCombat(player2);

        assertThat(findPermanent(player1, bearsId).isTapped()).isFalse();
    }
}
