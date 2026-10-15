package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ValleyDasher.class})
class ValleyDasherTest extends BaseCardTest {

    @Test
    @DisplayName("Valley Dasher must attack when able")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new ValleyDasher());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Valley Dasher can attack immediately because it has haste")
    void canAttackWithSummoningSickness() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ValleyDasher());

        declareAttackers(List.of(0));

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A newly entered Valley Dasher must attack because haste makes it able")
    void mustAttackWithSummoningSickness() {
        harness.addToBattlefield(player1, new ValleyDasher());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("A tapped Valley Dasher is not required to attack")
    void tappedDasherDoesNotHaveToAttack() {
        Permanent dasher = addCreatureReady(player1, new ValleyDasher());
        dasher.tap();
        harness.setLife(player2, 20);

        declareAttackers(List.of());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Attacking with one Valley Dasher does not satisfy another's requirement")
    void everyAbleDasherMustAttack() {
        addCreatureReady(player1, new ValleyDasher());
        addCreatureReady(player1, new ValleyDasher());
        harness.setLife(player2, 20);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");

        declareAttackers(List.of(0, 1));

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Valley Dasher must attack again when untapped for another combat")
    void mustAttackInEachCombat() {
        Permanent dasher = addCreatureReady(player1, new ValleyDasher());
        harness.setLife(player2, 20);
        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        dasher.untap();

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");

        declareAttackers(List.of(0));

        harness.assertLife(player2, 16);
    }
}
