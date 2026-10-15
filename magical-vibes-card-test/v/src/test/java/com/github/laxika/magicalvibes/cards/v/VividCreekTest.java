package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VividCreek.class})
class VividCreekTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped with two charge counters")
    void entersTappedWithTwoChargeCounters() {
        harness.setHand(player1, List.of(new VividCreek()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent creek = creek(player1);
        assertThat(creek.isTapped()).isTrue();
        assertThat(creek.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("First ability taps for blue mana without removing a counter")
    void tapForBlueMana() {
        Permanent creek = addReadyCreek(player1);
        creek.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(creek.isTapped()).isTrue();
        assertThat(creek.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty(); // mana ability does not use the stack
    }

    @Test
    @DisplayName("Second ability removes a charge counter and prompts for a color")
    void secondAbilityRemovesCounterAndPromptsForColor() {
        Permanent creek = addReadyCreek(player1);
        creek.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(creek.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(creek.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Choosing a color adds exactly one mana of that color")
    void choosingColorAddsMana() {
        Permanent creek = addReadyCreek(player1);
        creek.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED"})
    @DisplayName("The counter-removal ability can produce each remaining color")
    void producesOtherColors(ManaColor color) {
        Permanent creek = addReadyCreek(player1);
        creek.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());

        for (ManaColor poolColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(poolColor))
                    .isEqualTo(poolColor == color ? 1 : 0);
        }
        assertThat(creek.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(creek.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate the second ability with no charge counters")
    void cannotActivateSecondAbilityWithoutCounters() {
        Permanent creek = addReadyCreek(player1);
        creek.setCounterCount(CounterType.CHARGE, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Blue mana remains available after the last charge counter is spent")
    void blueManaRemainsAvailableAfterLastCounter() {
        Permanent creek = addReadyCreek(player1);
        creek.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(creek.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        creek.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(creek.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(creek.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"BLUE", "GREEN"})
    @DisplayName("Neither mana ability can be activated while tapped")
    void cannotActivateWhileTapped(ManaColor color) {
        Permanent creek = addReadyCreek(player1);
        creek.setCounterCount(CounterType.CHARGE, 2);
        creek.tap();
        int abilityIndex = color == ManaColor.BLUE ? 0 : 1;

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creek.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyCreek(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new VividCreek());
    }

    private Permanent creek(com.github.laxika.magicalvibes.model.Player player) {
        return findPermanent(player, "Vivid Creek");
    }
}
