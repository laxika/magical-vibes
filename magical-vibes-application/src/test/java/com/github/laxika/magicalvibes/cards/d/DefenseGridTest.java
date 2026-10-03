package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DefenseGrid.class, HolyDay.class, IcyManipulator.class})
class DefenseGridTest extends BaseCardTest {

    @Test
    @DisplayName("A spell is not taxed during its controller's own turn")
    void notTaxedOnOwnTurn() {
        harness.addToBattlefield(player1, new DefenseGrid());
        harness.forceActivePlayer(player1);
        // {W} is enough on the caster's own turn
        harness.castFromHand(player1, new HolyDay(), "{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("A spell cast on an opponent's turn costs {3} more")
    void taxedOnOpponentsTurn() {
        harness.addToBattlefield(player1, new DefenseGrid());
        harness.forceActivePlayer(player1);
        // {W} is not enough during player1's turn - needs {3}{W}
        assertThatThrownBy(() -> harness.castFromHand(player2, new HolyDay(), "{W}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A spell on an opponent's turn is castable with the extra {3}")
    void castableWithExtraManaOnOpponentsTurn() {
        harness.addToBattlefield(player1, new DefenseGrid());
        harness.forceActivePlayer(player1);
        harness.castFromHand(player2, new HolyDay(), "{3}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("The tax also applies to the controller's own spells on an opponent's turn")
    void controllerAlsoTaxedOffTurn() {
        harness.addToBattlefield(player1, new DefenseGrid());
        harness.forceActivePlayer(player2);
        // Even Defense Grid's controller pays {3} more when it's not their turn
        assertThatThrownBy(() -> harness.castFromHand(player1, new HolyDay(), "{W}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A spell is untaxed on its controller's turn even when another player controls Defense Grid")
    void untaxedWhenAnotherPlayerControlsGrid() {
        harness.addToBattlefield(player1, new DefenseGrid());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new HolyDay(), "{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Multiple Defense Grids add their taxes together")
    void multipleGridsStack() {
        harness.addToBattlefield(player1, new DefenseGrid());
        harness.addToBattlefield(player1, new DefenseGrid());
        harness.forceActivePlayer(player1);

        // {W} plus {3} for each Grid = {6}{W}
        harness.castFromHand(player2, new HolyDay(), "{6}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An activated ability is not taxed")
    void doesNotTaxActivatedAbilities() {
        Permanent grid = harness.addToBattlefieldAndReturn(player1, new DefenseGrid());
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.forceActivePlayer(player1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 0, null, grid.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A tapped Defense Grid still taxes spells")
    void tappedGridStillTaxesSpells() {
        harness.addToBattlefieldAndReturn(player1, new DefenseGrid()).setTapped(true);
        harness.forceActivePlayer(player1);

        harness.castFromHand(player2, new HolyDay(), "{3}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Defense Grid does not tax spells while it is on the stack")
    void gridOnStackDoesNotTaxSpells() {
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new DefenseGrid(), "{2}");

        harness.castFromHand(player2, new HolyDay(), "{W}");

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Grids controlled by different players both tax an off-turn spell")
    void gridsWithDifferentControllersStack() {
        harness.addToBattlefield(player1, new DefenseGrid());
        harness.addToBattlefield(player2, new DefenseGrid());
        harness.forceActivePlayer(player1);

        harness.castFromHand(player2, new HolyDay(), "{6}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Paying only one Grid's tax is insufficient when two Grids are present")
    void oneTaxIsInsufficientForTwoGrids() {
        harness.addToBattlefield(player1, new DefenseGrid());
        harness.addToBattlefield(player2, new DefenseGrid());
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castFromHand(player2, new HolyDay(), "{3}{W}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
