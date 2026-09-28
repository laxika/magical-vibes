package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SarahJaneSmith;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JoGrant.class, SarahJaneSmith.class, GrizzlyBears.class})
class JoGrantTest extends BaseCardTest {

    @Test
    @DisplayName("Historic cards in your hand gain cycling and cycling puts a counter on Jo Grant")
    void historicCardGainsCyclingAndTriggersCounter() {
        Permanent jo = harness.addToBattlefieldAndReturn(player1, new JoGrant());
        harness.setHand(player1, List.of(new SarahJaneSmith()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(jo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Sarah Jane Smith");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Jo Grant does not grant cycling to nonhistoric cards")
    void nonhistoricCardDoesNotGainCycling() {
        harness.addToBattlefield(player1, new JoGrant());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card has no hand-activated ability");
    }

    @Test
    @DisplayName("Jo Grant only grants cycling in its controller's hand")
    void opponentHistoricCardDoesNotGainCycling() {
        harness.addToBattlefield(player1, new JoGrant());
        harness.setHand(player2, List.of(new SarahJaneSmith()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateHandAbility(player2, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card has no hand-activated ability");
    }
}
