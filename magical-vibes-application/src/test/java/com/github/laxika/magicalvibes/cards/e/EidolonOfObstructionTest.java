package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AjaniGoldmane;
import com.github.laxika.magicalvibes.cards.a.AdantoVanguard;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EidolonOfObstruction.class, AjaniGoldmane.class, AdantoVanguard.class})
class EidolonOfObstructionTest extends BaseCardTest {

    @Test
    @DisplayName("Taxes an opponent's planeswalker loyalty ability")
    void taxesOpponentsPlaneswalkerLoyaltyAbility() {
        harness.addToBattlefield(player1, new EidolonOfObstruction());
        Permanent ajani = addReadyAjani(player2);
        prepareTurn(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.getLife(player2.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Does not tax the controller's planeswalker or a nonloyalty ability")
    void doesNotTaxControllersPlaneswalkerOrNonloyaltyAbility() {
        harness.addToBattlefield(player1, new EidolonOfObstruction());
        Permanent ajani = addReadyAjani(player1);
        prepareTurn(player1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);

        harness.addToBattlefield(player2, new AdantoVanguard());
        prepareTurn(player2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    private Permanent addReadyAjani(com.github.laxika.magicalvibes.model.Player player) {
        Permanent ajani = new Permanent(new AjaniGoldmane());
        ajani.setCounterCount(CounterType.LOYALTY, 4);
        ajani.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(ajani);
        return ajani;
    }

    private void prepareTurn(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
