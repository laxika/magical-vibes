package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KarplusanForest.class})
class KarplusanForestTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless mana adds {C} and deals no damage")
    void tapForColorlessMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KarplusanForest());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Tapping for red mana adds {R} and deals 1 damage to controller")
    void tapForRedMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KarplusanForest());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Tapping for green mana adds {G} and deals 1 damage to controller")
    void tapForGreenMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KarplusanForest());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Colored mana abilities damage only their controller")
    void coloredManaDamagesOnlyController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 17);
        harness.addToBattlefield(player1, new KarplusanForest());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new KarplusanForest());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Colored mana abilities are mana abilities and do not use the stack")
    void coloredManaAbilitiesDoNotUseStack() {
        harness.addToBattlefield(player1, new KarplusanForest());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Untapping allows another mana choice and each colored activation deals damage")
    void canChooseDifferentColorAfterUntapping() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KarplusanForest());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.performUntapStep(player1);
        assertThat(land.isTapped()).isFalse();
        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.assertLife(player1, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The other player's forest adds mana and deals damage to that player")
    void otherPlayersForestDamagesItsController() {
        harness.forceActivePlayer(player2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 17);
        harness.addToBattlefield(player2, new KarplusanForest());

        harness.activateAbility(player2, 0, 2, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
        assertThat(gd.stack).isEmpty();
    }
}
