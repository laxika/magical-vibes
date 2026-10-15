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

@CardUsed({VividCrag.class})
class VividCragTest extends BaseCardTest {


    @Test
    @DisplayName("Enters the battlefield tapped with two charge counters")
    void entersTappedWithTwoChargeCounters() {
        harness.setHand(player1, List.of(new VividCrag()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent crag = crag(player1);
        assertThat(crag.isTapped()).isTrue();
        assertThat(crag.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }


    @Test
    @DisplayName("First ability taps for red mana without removing a counter")
    void tapForRedMana() {
        Permanent crag = addReadyCrag(player1);
        crag.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(crag.isTapped()).isTrue();
        assertThat(crag.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty(); // mana ability does not use the stack
    }


    @Test
    @DisplayName("Second ability removes a charge counter and prompts for a color")
    void secondAbilityRemovesCounterAndPromptsForColor() {
        Permanent crag = addReadyCrag(player1);
        crag.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(crag.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(crag.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Choosing a color adds exactly one mana of that color")
    void choosingColorAddsMana() {
        Permanent crag = addReadyCrag(player1);
        crag.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate the second ability with no charge counters")
    void cannotActivateSecondAbilityWithoutCounters() {
        Permanent crag = addReadyCrag(player1);
        crag.setCounterCount(CounterType.CHARGE, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(crag.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The final charge counter can produce any of the five colors")
    void finalCounterProducesEachColor() {
        Permanent crag = addReadyCrag(player1);
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            crag.untap();
            crag.setCounterCount(CounterType.CHARGE, 1);

            harness.activateAbility(player1, 0, 1, null, null);
            harness.handleListChoice(player1, color.name());

            assertThat(crag.getCounterCount(CounterType.CHARGE)).isZero();
            assertThat(crag.isTapped()).isTrue();
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
            assertThat(gd.stack).isEmpty();
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    @Test
    @DisplayName("Can still produce red mana after all charge counters are gone")
    void producesRedWithoutCounters() {
        Permanent crag = addReadyCrag(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(crag.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(crag.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped Crag cannot activate either ability or spend a counter")
    void tappedCragCannotActivateEitherAbility() {
        Permanent crag = addReadyCrag(player1);
        crag.setCounterCount(CounterType.CHARGE, 2);
        crag.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(crag.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyCrag(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new VividCrag());
    }

    private Permanent crag(com.github.laxika.magicalvibes.model.Player player) {
        return findPermanent(player, "Vivid Crag");
    }
}
