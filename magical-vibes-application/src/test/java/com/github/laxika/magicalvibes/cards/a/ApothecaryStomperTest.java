package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApothecaryStomperTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mode puts two +1/+1 counters on a creature you control")
    void etbPutsTwoCountersOnOwnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castWithCounterMode(bears.getId());
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("ETB mode gains 4 life")
    void etbGainsFourLife() {
        harness.setLife(player1, 5);

        harness.setHand(player1, List.of(new ApothecaryStomper()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(9);
    }

    @Test
    @DisplayName("Counter mode cannot target an opponent's creature")
    void counterModeCannotTargetOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ApothecaryStomper()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    private void castWithCounterMode(UUID targetId) {
        harness.setHand(player1, List.of(new ApothecaryStomper()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
