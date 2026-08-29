package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AdantoVanguard;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.y.YavimayaCoast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TitheTakerTest extends BaseCardTest {

    @Test
    @DisplayName("Taxes an opponent's spell during the controller's turn")
    void taxesOpponentsSpellDuringControllersTurn() {
        harness.addToBattlefield(player1, new TitheTaker());
        prepareTurn(player1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not tax an opponent's spell during that opponent's turn")
    void doesNotTaxOpponentsSpellDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new TitheTaker());
        prepareTurn(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Taxes an opponent's non-mana ability")
    void taxesOpponentsNonManaAbility() {
        harness.addToBattlefield(player1, new TitheTaker());
        harness.addToBattlefield(player2, new AdantoVanguard());
        prepareTurn(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not tax an opponent's mana ability")
    void doesNotTaxOpponentsManaAbility() {
        harness.addToBattlefield(player1, new TitheTaker());
        harness.addToBattlefield(player2, new YavimayaCoast());
        prepareTurn(player1);

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Afterlife creates a Spirit token when Tithe Taker dies")
    void afterlifeCreatesSpiritWhenItDies() {
        Permanent titheTaker = harness.addToBattlefieldAndReturn(player1, new TitheTaker());
        prepareTurn(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, titheTaker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Tithe Taker");
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
    }

    private void prepareTurn(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
