package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TabletOfCompleation.class})
class TabletOfCompleationTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability puts an oil counter on the Tablet")
    void tapAbilityPutsOilCounter() {
        Permanent tablet = harness.addToBattlefieldAndReturn(player1, new TabletOfCompleation());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(tablet.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Colorless mana ability requires two oil counters")
    void colorlessManaAbilityRequiresTwoOilCounters() {
        Permanent tablet = harness.addToBattlefieldAndReturn(player1, new TabletOfCompleation());
        tablet.setCounterCount(CounterType.OIL, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("oil counters");
    }

    @Test
    @DisplayName("Colorless mana ability adds one colorless mana with two oil counters")
    void colorlessManaAbilityAddsMana() {
        Permanent tablet = harness.addToBattlefieldAndReturn(player1, new TabletOfCompleation());
        tablet.setCounterCount(CounterType.OIL, 2);
        int before = gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(before + 1);
    }

    @Test
    @DisplayName("Draw ability requires five oil counters and draws a card when enabled")
    void drawAbilityRequiresFiveOilCountersAndDraws() {
        Permanent tablet = harness.addToBattlefieldAndReturn(player1, new TabletOfCompleation());
        tablet.setCounterCount(CounterType.OIL, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("oil counters");

        tablet.setCounterCount(CounterType.OIL, 5);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new TabletOfCompleation()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Oil counters accumulate on resolution and tapping is paid immediately")
    void oilCountersAccumulateOnResolution() {
        Permanent tablet = harness.addToBattlefieldAndReturn(player1, new TabletOfCompleation());
        tablet.setCounterCount(CounterType.OIL, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(tablet.isTapped()).isTrue();
        assertThat(tablet.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(tablet.getCounterCount(CounterType.OIL)).isEqualTo(3);
    }

    @Test
    @DisplayName("Mana ability resolves immediately and does not spend oil counters")
    void manaAbilityDoesNotUseStackOrSpendCounters() {
        Permanent tablet = harness.addToBattlefieldAndReturn(player1, new TabletOfCompleation());
        tablet.setCounterCount(CounterType.OIL, 6);
        int before = gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(tablet.isTapped()).isTrue();
        assertThat(tablet.getCounterCount(CounterType.OIL)).isEqualTo(6);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(before + 1);
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Draw ability still resolves if oil counters fall below five after activation")
    void drawResolvesAfterOilCountersAreRemoved() {
        Permanent tablet = harness.addToBattlefieldAndReturn(player1, new TabletOfCompleation());
        tablet.setCounterCount(CounterType.OIL, 6);
        TabletOfCompleation drawnCard = new TabletOfCompleation();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(tablet.isTapped()).isTrue();
        assertThat(tablet.getCounterCount(CounterType.OIL)).isEqualTo(6);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        tablet.setCounterCount(CounterType.OIL, 0);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Draw ability cannot be activated without paying one mana")
    void drawRequiresManaPayment() {
        Permanent tablet = harness.addToBattlefieldAndReturn(player1, new TabletOfCompleation());
        tablet.setCounterCount(CounterType.OIL, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(tablet.isTapped()).isFalse();
        assertThat(tablet.getCounterCount(CounterType.OIL)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }
}
