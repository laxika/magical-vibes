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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VividGrove.class})
class VividGroveTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped with two charge counters")
    void entersTappedWithTwoChargeCounters() {
        harness.setHand(player1, List.of(new VividGrove()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent grove = grove(player1);
        assertThat(grove.isTapped()).isTrue();
        assertThat(grove.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("First ability taps for green mana without removing a counter")
    void tapForGreenMana() {
        Permanent grove = addReadyGrove(player1);
        grove.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(grove.isTapped()).isTrue();
        assertThat(grove.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty(); // mana ability does not use the stack
    }

    @Test
    @DisplayName("Second ability removes a charge counter and prompts for a color")
    void secondAbilityRemovesCounterAndPromptsForColor() {
        Permanent grove = addReadyGrove(player1);
        grove.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(grove.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(grove.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Choosing a color adds exactly one mana of that color")
    void choosingColorAddsMana() {
        Permanent grove = addReadyGrove(player1);
        grove.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate the second ability with no charge counters")
    void cannotActivateSecondAbilityWithoutCounters() {
        Permanent grove = addReadyGrove(player1);
        grove.setCounterCount(CounterType.CHARGE, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(grove.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("The counter ability can produce each of the five colors")
    void producesEveryColor() {
        Permanent grove = addReadyGrove(player1);

        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            grove.untap();
            grove.setCounterCount(CounterType.CHARGE, 1);
            harness.activateAbility(player1, 0, 1, null, null);
            harness.handleListChoice(player1, color.name());

            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
            assertThat(grove.getCounterCount(CounterType.CHARGE)).isZero();
            assertThat(grove.isTapped()).isTrue();
            assertThat(gd.stack).isEmpty();
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(5);
    }

    @Test
    @DisplayName("Green mana remains available after the final charge counter is spent")
    void producesGreenAfterLastCounterIsSpent() {
        Permanent grove = addReadyGrove(player1);
        grove.setCounterCount(CounterType.CHARGE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");
        grove.untap();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(grove.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(grove.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Grove cannot pay either tap cost")
    void cannotActivateEitherAbilityWhileTapped() {
        Permanent grove = addReadyGrove(player1);
        grove.setCounterCount(CounterType.CHARGE, 2);
        grove.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(grove.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyGrove(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new VividGrove());
    }

    private Permanent grove(com.github.laxika.magicalvibes.model.Player player) {
        return findPermanent(player, "Vivid Grove");
    }
}
