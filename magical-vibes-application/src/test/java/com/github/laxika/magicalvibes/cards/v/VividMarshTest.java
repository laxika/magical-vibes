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

@CardUsed({VividMarsh.class})
class VividMarshTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped with two charge counters")
    void entersTappedWithTwoChargeCounters() {
        harness.setHand(player1, List.of(new VividMarsh()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent marsh = marsh(player1);
        assertThat(marsh.isTapped()).isTrue();
        assertThat(marsh.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("First ability taps for black mana without removing a counter")
    void tapForBlackMana() {
        Permanent marsh = addReadyMarsh(player1);
        marsh.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(marsh.isTapped()).isTrue();
        assertThat(marsh.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty(); // mana ability does not use the stack
    }

    @Test
    @DisplayName("Second ability removes a charge counter and prompts for a color")
    void secondAbilityRemovesCounterAndPromptsForColor() {
        Permanent marsh = addReadyMarsh(player1);
        marsh.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(marsh.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(marsh.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Choosing a color adds exactly one mana of that color")
    void choosingColorAddsMana() {
        Permanent marsh = addReadyMarsh(player1);
        marsh.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate the second ability with no charge counters")
    void cannotActivateSecondAbilityWithoutCounters() {
        Permanent marsh = addReadyMarsh(player1);
        marsh.setCounterCount(CounterType.CHARGE, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(marsh.isTapped()).isFalse();
        assertThat(marsh.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("The last charge counter can produce any of the five colors")
    void lastChargeCounterProducesChosenColor(ManaColor color) {
        Permanent marsh = addReadyMarsh(player1);
        marsh.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());

        for (ManaColor manaColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor))
                    .isEqualTo(manaColor == color ? 1 : 0);
        }
        assertThat(marsh.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(marsh.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();

        marsh.untap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK))
                .isEqualTo(color == ManaColor.BLACK ? 2 : 1);
        assertThat(marsh.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Neither mana ability can be activated while the land is tapped")
    void cannotActivateWhileTapped() {
        Permanent marsh = harness.enterBattlefieldAndReturn(player1, new VividMarsh());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(marsh.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyMarsh(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new VividMarsh());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent marsh(com.github.laxika.magicalvibes.model.Player player) {
        return findPermanent(player, "Vivid Marsh");
    }
}
