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

@CardUsed({VividMeadow.class})
class VividMeadowTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped with two charge counters")
    void entersTappedWithTwoChargeCounters() {
        harness.setHand(player1, List.of(new VividMeadow()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent meadow = meadow(player1);
        assertThat(meadow.isTapped()).isTrue();
        assertThat(meadow.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("First ability taps for white mana without removing a counter")
    void tapForWhiteMana() {
        Permanent meadow = addReadyMeadow(player1);
        meadow.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(meadow.isTapped()).isTrue();
        assertThat(meadow.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty(); // mana ability does not use the stack
    }

    @Test
    @DisplayName("Second ability removes a charge counter and prompts for a color")
    void secondAbilityRemovesCounterAndPromptsForColor() {
        Permanent meadow = addReadyMeadow(player1);
        meadow.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(meadow.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(meadow.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Choosing a color adds exactly one mana of that color")
    void choosingColorAddsMana(ManaColor color) {
        Permanent meadow = addReadyMeadow(player1);
        meadow.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate the second ability with no charge counters")
    void cannotActivateSecondAbilityWithoutCounters() {
        Permanent meadow = addReadyMeadow(player1);
        meadow.setCounterCount(CounterType.CHARGE, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An exhausted meadow can still produce white mana")
    void whiteManaRemainsAvailableAfterBothCountersAreSpent() {
        Permanent meadow = addReadyMeadow(player1);
        meadow.setCounterCount(CounterType.CHARGE, 2);

        for (int i = 0; i < 2; i++) {
            meadow.untap();
            harness.activateAbility(player1, 0, 1, null, null);
            harness.handleListChoice(player1, "BLUE");
        }

        assertThat(meadow.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        meadow.untap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(meadow.isTapped()).isFalse();
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(meadow.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped meadow cannot activate either mana ability")
    void tappedMeadowCannotProduceMana() {
        Permanent meadow = addReadyMeadow(player1);
        meadow.setCounterCount(CounterType.CHARGE, 2);
        meadow.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(meadow.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    private Permanent addReadyMeadow(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new VividMeadow());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent meadow(com.github.laxika.magicalvibes.model.Player player) {
        return findPermanent(player, "Vivid Meadow");
    }
}
