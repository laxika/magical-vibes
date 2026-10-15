package com.github.laxika.magicalvibes.cards.m;

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

@CardUsed(MercadianBazaar.class)
class MercadianBazaarTest extends BaseCardTest {

    @Test
    @DisplayName("Mercadian Bazaar enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new MercadianBazaar()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Mercadian Bazaar").isTapped()).isTrue();
    }

    @Test
    @DisplayName("The first ability puts a storage counter on the land")
    void storesACounter() {
        Permanent bazaar = addBazaarWithCounters(0);
        bazaar.untap();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(bazaar.getCounterCount(CounterType.STORAGE)).isEqualTo(1);
        assertThat(bazaar.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing all storage counters adds that much red mana")
    void removingAllCountersAddsRed() {
        Permanent bazaar = addBazaarWithCounters(3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "3");

        assertThat(redMana()).isEqualTo(3);
        assertThat(bazaar.getCounterCount(CounterType.STORAGE)).isZero();
        assertThat(bazaar.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing fewer counters than present keeps the rest")
    void removingSomeCountersKeepsTheRest() {
        Permanent bazaar = addBazaarWithCounters(3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "1");

        assertThat(redMana()).isEqualTo(1);
        assertThat(bazaar.getCounterCount(CounterType.STORAGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing zero counters produces no mana but still taps the land")
    void removingZeroCountersProducesNoMana() {
        Permanent bazaar = addBazaarWithCounters(3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "0");

        assertThat(redMana()).isZero();
        assertThat(bazaar.getCounterCount(CounterType.STORAGE)).isEqualTo(3);
        assertThat(bazaar.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating with no storage counters produces no mana and no choice")
    void activatingWithNoCountersProducesNoMana() {
        Permanent bazaar = addBazaarWithCounters(0);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(redMana()).isZero();
        assertThat(bazaar.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The mana ability cannot be activated while Mercadian Bazaar is tapped")
    void cannotActivateManaAbilityWhileTapped() {
        Permanent bazaar = addBazaarWithCounters(3);
        bazaar.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Storing a counter uses the stack even on a newly entered land")
    void storageCounterIsAddedOnlyOnResolution() {
        Permanent bazaar = addBazaarWithCounters(0);
        bazaar.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(bazaar.isTapped()).isTrue();
        assertThat(bazaar.getCounterCount(CounterType.STORAGE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(bazaar.getCounterCount(CounterType.STORAGE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The mana ability resolves without the stack on a newly entered land")
    void manaAbilityDoesNotUseTheStack() {
        Permanent bazaar = addBazaarWithCounters(2);
        bazaar.setSummoningSick(true);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "2");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(redMana()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
        assertThat(bazaar.getCounterCount(CounterType.STORAGE)).isZero();
        assertThat(bazaar.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The storage ability cannot be activated while the land is tapped")
    void cannotStoreWhileTapped() {
        Permanent bazaar = addBazaarWithCounters(0);
        bazaar.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(bazaar.getCounterCount(CounterType.STORAGE)).isZero();
    }

    private Permanent addBazaarWithCounters(int counters) {
        Permanent bazaar = harness.addToBattlefieldAndReturn(player1, new MercadianBazaar());
        bazaar.setSummoningSick(false);
        if (counters > 0) {
            bazaar.setCounterCount(CounterType.STORAGE, counters);
        }
        return bazaar;
    }

    private int redMana() {
        return gd.playerManaPools.get(player1.getId()).get(ManaColor.RED);
    }
}
