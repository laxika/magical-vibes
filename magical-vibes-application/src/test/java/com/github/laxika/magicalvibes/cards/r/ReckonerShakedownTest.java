package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.s.SleekSchooner;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReckonerShakedown.class, Forest.class, GrizzlyBears.class, Peek.class, SleekSchooner.class})
class ReckonerShakedownTest extends BaseCardTest {

    @Test
    void choosesNonlandCardToDiscard() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Peek(), new Forest()));
        cast();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Peek");
        harness.assertInHand(player2, "Forest");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void decliningChoicePutsCountersOnCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Peek()));
        cast();

        harness.handleCardChosen(player1, -1);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInHand(player2, "Peek");
    }

    @Test
    void noNonlandCardUsesCounterModeWithoutPrompt() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Forest()));
        cast();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInHand(player2, "Forest");
    }

    @Test
    void canPutCountersOnVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new SleekSchooner());
        harness.setHand(player2, List.of(new Forest()));
        cast();

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void canOnlyTargetOpponent() {
        harness.setHand(player1, List.of(new ReckonerShakedown()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    private void cast() {
        harness.setHand(player1, List.of(new ReckonerShakedown()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
