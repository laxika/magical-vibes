package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TalismanOfIndulgence.class)
class TalismanOfIndulgenceTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless mana adds {C} and deals no damage")
    void tapForColorlessMana() {
        Permanent talisman = harness.addToBattlefieldAndReturn(player1, new TalismanOfIndulgence());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(talisman.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("Tapping for black mana adds {B} and deals 1 damage to controller")
    void tapForBlackMana() {
        Permanent talisman = harness.addToBattlefieldAndReturn(player1, new TalismanOfIndulgence());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(talisman.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    @DisplayName("Tapping for red mana adds {R} and deals 1 damage to controller")
    void tapForRedMana() {
        Permanent talisman = harness.addToBattlefieldAndReturn(player1, new TalismanOfIndulgence());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(talisman.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    @DisplayName("Colored mana damages only the controller")
    void coloredManaDamagesOnlyController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 17);
        harness.addToBattlefieldAndReturn(player1, new TalismanOfIndulgence());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("The other controller receives the colored mana and damage")
    void coloredManaForOtherController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 17);
        Permanent talisman = harness.addToBattlefieldAndReturn(player2, new TalismanOfIndulgence());

        harness.activateAbility(player2, 0, 1, null, null);
        harness.handleListChoice(player2, "BLACK");

        assertThat(talisman.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("The colored mana ability rejects colors other than black or red")
    void cannotChooseGreenMana() {
        harness.setLife(player1, 20);
        harness.addToBattlefieldAndReturn(player1, new TalismanOfIndulgence());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.handleListChoice(player1, "GREEN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid mana color choice");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        harness.assertLife(player1, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefieldAndReturn(player1, new TalismanOfIndulgence());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
