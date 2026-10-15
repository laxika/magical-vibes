package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThallidShellDweller.class})
class ThallidShellDwellerTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger adds a spore counter")
    void upkeepTriggerAddsSporeCounter() {
        Permanent shellDweller = addShellDweller();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(shellDweller.getCounterCount(CounterType.valueOf("SPORE"))).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing three spore counters creates a Saproling token")
    void removesThreeSporeCountersAndCreatesToken() {
        Permanent shellDweller = addShellDweller();
        shellDweller.setCounterCount(CounterType.valueOf("SPORE"), 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shellDweller.getCounterCount(CounterType.valueOf("SPORE"))).isZero();
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("The token ability requires three spore counters")
    void tokenAbilityRequiresThreeSporeCounters() {
        addShellDweller().setCounterCount(CounterType.valueOf("SPORE"), 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fungus counters cannot pay a spore counter cost")
    void fungusCountersCannotPayTokenAbilityCost() {
        Permanent shellDweller = addShellDweller();
        shellDweller.setCounterCount(CounterType.FUNGUS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(shellDweller.getCounterCount(CounterType.FUNGUS)).isEqualTo(3);
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("Opponent's upkeep does not add a counter")
    void opponentsUpkeepDoesNotAddCounter() {
        Permanent shellDweller = addShellDweller();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(shellDweller.getCounters()).isEmpty();
    }

    @Test
    @DisplayName("A tapped summoning-sick creature can activate and pays before resolution")
    void tappedSummoningSickCreatureCanActivate() {
        Permanent shellDweller = harness.addToBattlefieldAndReturn(player1, new ThallidShellDweller());
        shellDweller.setSummoningSick(true);
        shellDweller.tap();
        shellDweller.setCounterCount(CounterType.valueOf("SPORE"), 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(shellDweller.getCounterCount(CounterType.valueOf("SPORE"))).isEqualTo(1);
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
        assertThat(shellDweller.isTapped()).isTrue();
    }

    private Permanent addShellDweller() {
        Permanent shellDweller = harness.addToBattlefieldAndReturn(player1, new ThallidShellDweller());
        shellDweller.setSummoningSick(false);
        return shellDweller;
    }
}
