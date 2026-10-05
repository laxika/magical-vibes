package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NornsInquisitor.class})
class NornsInquisitorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with an Incubator token with two +1/+1 counters")
    void entersWithIncubatorToken() {
        castNornsInquisitor();
        resolveAllTriggers();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on the transformed Incubator")
    void putsCounterOnTransformedIncubator() {
        castNornsInquisitor();
        resolveAllTriggers();

        Permanent incubator = findPermanent(player1, "Incubator");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(incubator), null, null);
        resolveAllTriggers();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(findPermanent(player1, "Norn's Inquisitor")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each allied Inquisitor adds its own counter to the transformed permanent")
    void multipleInquisitorsEachAddCounter() {
        castNornsInquisitor();
        resolveAllTriggers();
        harness.addToBattlefield(player1, new NornsInquisitor());

        Permanent incubator = findPermanent(player1, "Incubator");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(incubator), null, null);
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(findPermanents(player1, "Norn's Inquisitor"))
                .allSatisfy(inquisitor -> assertThat(inquisitor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    @DisplayName("An opponent's Inquisitor does not trigger for your transformation")
    void opposingInquisitorDoesNotAddCounter() {
        castNornsInquisitor();
        resolveAllTriggers();
        Permanent opposingInquisitor = harness.addToBattlefieldAndReturn(player2, new NornsInquisitor());

        Permanent incubator = findPermanent(player1, "Incubator");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(incubator), null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(opposingInquisitor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Two pending transform activations transform the Incubator only once")
    void pendingTransformActivationsOnlyTriggerOnce() {
        castNornsInquisitor();
        resolveAllTriggers();

        Permanent incubator = findPermanent(player1, "Incubator");
        int incubatorIndex = gd.playerBattlefields.get(player1.getId()).indexOf(incubator);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, incubatorIndex, null, null);
        harness.activateAbility(player1, incubatorIndex, null, null);
        resolveAllTriggers();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private void castNornsInquisitor() {
        harness.setHand(player1, List.of(new NornsInquisitor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }
}
