package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BanditsHaul.class, Shock.class})
class BanditsHaulTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a loot counter on itself when its controller commits a crime")
    void crimeAddsLootCounter() {
        Permanent haul = harness.addToBattlefieldAndReturn(player1, new BanditsHaul());

        commitCrime();

        assertThat(haul.getCounterCount(CounterType.LOOT)).isEqualTo(1);
    }

    @Test
    @DisplayName("The crime trigger fires only once each turn")
    void crimeTriggerFiresOnlyOnceEachTurn() {
        Permanent haul = harness.addToBattlefieldAndReturn(player1, new BanditsHaul());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        castCrimeSpell();
        castCrimeSpell();

        assertThat(haul.getCounterCount(CounterType.LOOT)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing two loot counters draws a card")
    void removingTwoLootCountersDrawsCard() {
        Permanent haul = harness.addToBattlefieldAndReturn(player1, new BanditsHaul());
        haul.setCounterCount(CounterType.LOOT, 2);
        harness.setLibrary(player1, List.of(new Shock()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(haul.getCounterCount(CounterType.LOOT)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(haul.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The draw ability requires two loot counters")
    void drawAbilityRequiresTwoLootCounters() {
        Permanent haul = harness.addToBattlefieldAndReturn(player1, new BanditsHaul());
        haul.setCounterCount(CounterType.LOOT, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tapping it adds one mana of the chosen color")
    void tapsForAnyColor() {
        Permanent haul = harness.addToBattlefieldAndReturn(player1, new BanditsHaul());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(haul.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private void commitCrime() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        castCrimeSpell();
    }

    private void castCrimeSpell() {
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
