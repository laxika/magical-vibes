package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrnitharchTest extends BaseCardTest {

    @Test
    @DisplayName("The opponent pays tribute and Ornitharch enters with two +1/+1 counters")
    void opponentPaysTribute() {
        castOrnitharch();

        assertThatThrownBy(() -> harness.handleMayAbilityChosen(player1, true))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMayAbilityChosen(player2, true);

        Permanent ornitharch = findPermanent(player1, "Ornitharch");
        assertThat(ornitharch.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanents(player1, "Bird")).isEmpty();
    }

    @Test
    @DisplayName("Declining tribute creates two 1/1 white Bird tokens with flying")
    void opponentDeclinesTribute() {
        castOrnitharch();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bird")).hasSize(2);
        assertThat(findPermanent(player1, "Ornitharch")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castOrnitharch() {
        harness.setHand(player1, java.util.List.of(new Ornitharch()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
