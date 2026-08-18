package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FeastOnTheFallenTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on a creature you control when an opponent lost life last turn")
    void putsCounterOnControlledCreatureAfterOpponentLostLife() {
        harness.addToBattlefield(player1, new FeastOnTheFallen());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");

        gd.lifeLostLastTurn.put(player2.getId(), 2);
        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bearsId)
                .doesNotContain(opponentBearsId);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(bearsId))
                .findFirst()
                .orElseThrow();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when only the controller lost life last turn")
    void doesNotTriggerWhenControllerLostLife() {
        harness.addToBattlefield(player1, new FeastOnTheFallen());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        gd.lifeLostLastTurn.put(player1.getId(), 2);
        advanceToUpkeep(player1);

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(bearsId))
                .findFirst()
                .orElseThrow();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
