package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(BloodCelebrant.class)
class BloodCelebrantTest extends BaseCardTest {

    @Test
    @DisplayName("Paying black mana and 1 life adds one mana of the chosen color")
    void addsChosenManaAndPaysLife() {
        harness.addToBattlefield(player1, new BloodCelebrant());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without black mana")
    void cannotActivateWithoutBlackMana() {
        harness.addToBattlefield(player1, new BloodCelebrant());
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough life")
    void cannotActivateWithoutEnoughLife() {
        harness.addToBattlefield(player1, new BloodCelebrant());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
