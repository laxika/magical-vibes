package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(PhyrexianLens.class)
class PhyrexianLensTest extends BaseCardTest {

    @Test
    @DisplayName("Ability pays 1 life and adds one mana of the chosen color")
    void abilityPaysLifeAndAddsChosenColor() {
        Permanent lens = harness.addToBattlefieldAndReturn(player1, new PhyrexianLens());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertLife(player1, 19);
        assertThat(lens.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Ability cannot be activated without enough life to pay")
    void cannotActivateWithoutEnoughLife() {
        harness.addToBattlefield(player1, new PhyrexianLens());
        harness.setLife(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Mana ability resolves without using the stack for every color")
    void producesEveryColorWithoutUsingStack(ManaColor color) {
        Permanent lens = harness.addToBattlefieldAndReturn(player1, new PhyrexianLens());
        harness.setLife(player1, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 1);
        assertThat(lens.isTapped()).isTrue();

        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 1);
    }

    @Test
    @DisplayName("A tapped Lens cannot activate or charge life again")
    void cannotActivateTappedLens() {
        Permanent lens = harness.addToBattlefieldAndReturn(player1, new PhyrexianLens());
        lens.setTapped(true);
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        assertThat(lens.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
