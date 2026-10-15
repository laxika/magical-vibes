package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TalismanOfCreativity.class)
class TalismanOfCreativityTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless mana adds {C} and deals no damage")
    void tapForColorlessMana() {
        Permanent talisman = harness.addToBattlefieldAndReturn(player1, new TalismanOfCreativity());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(talisman.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Tapping for blue mana adds {U} and deals 1 damage to controller")
    void tapForBlueMana() {
        Permanent talisman = harness.addToBattlefieldAndReturn(player1, new TalismanOfCreativity());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(talisman.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Tapping for red mana adds {R} and deals 1 damage to controller")
    void tapForRedMana() {
        Permanent talisman = harness.addToBattlefieldAndReturn(player1, new TalismanOfCreativity());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(talisman.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new TalismanOfCreativity());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Colored mana damages its controller and records the artifact as the damage source")
    void coloredManaDamagesOnlyItsController() {
        harness.forceActivePlayer(player2);
        Permanent talisman = harness.addToBattlefieldAndReturn(player2, new TalismanOfCreativity());
        int controllerLife = gd.playerLifeTotals.get(player2.getId());
        int opponentLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player2, 0, 1, null, null);
        harness.handleListChoice(player2, "RED");

        harness.assertLife(player2, controllerLife - 1);
        harness.assertLife(player1, opponentLife);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.damageDealtThisTurnBySource.get(talisman.getId())).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The colored mana ability cannot produce green mana")
    void cannotChooseAnUnlistedColor() {
        harness.addToBattlefield(player1, new TalismanOfCreativity());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.handleListChoice(player1, "GREEN"))
                .isInstanceOf(IllegalArgumentException.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    @DisplayName("Colored mana can be activated at one life because damage is not a life payment")
    void coloredManaAtOneLife() {
        harness.addToBattlefield(player1, new TalismanOfCreativity());
        harness.setLife(player1, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        harness.assertLife(player1, 0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.stack).isEmpty();
    }
}
