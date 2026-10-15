package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AjaniGoldmane;
import com.github.laxika.magicalvibes.cards.a.AdantoVanguard;
import com.github.laxika.magicalvibes.cards.i.Ichthyomorphosis;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EidolonOfObstruction.class, AjaniGoldmane.class, AdantoVanguard.class,
        ElspethSunsNemesis.class, Ichthyomorphosis.class})
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
        Permanent ajani = harness.addToBattlefieldAndReturn(player, new AjaniGoldmane());
        ajani.setCounterCount(CounterType.LOYALTY, 4);
        ajani.setSummoningSick(false);
        return ajani;
    }

    @Test
    @CardUsed({EidolonOfObstruction.class, ElspethSunsNemesis.class})
    void multipleEidolonsStackTheirManaTaxesWithoutIncreasingLoyaltyCost() {
        harness.addToBattlefield(player1, new EidolonOfObstruction());
        harness.addToBattlefield(player1, new EidolonOfObstruction());
        Permanent elspeth = harness.addToBattlefieldAndReturn(player2, new ElspethSunsNemesis());
        elspeth.setCounterCount(CounterType.LOYALTY, 5);
        prepareTurn(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, 2, null, null);
        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(25);
    }

    @Test
    @CardUsed({EidolonOfObstruction.class, ElspethSunsNemesis.class})
    void taxStopsWhenEidolonLeavesBattlefield() {
        Permanent eidolon = harness.addToBattlefieldAndReturn(player1, new EidolonOfObstruction());
        Permanent elspeth = harness.addToBattlefieldAndReturn(player2, new ElspethSunsNemesis());
        elspeth.setCounterCount(CounterType.LOYALTY, 5);
        prepareTurn(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
        gd.playerBattlefields.get(player1.getId()).remove(eidolon);
        gd.playerGraveyards.get(player1.getId()).add(eidolon.getCard());

        harness.activateAbility(player2, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(25);
    }

    @Test
    @CardUsed({EidolonOfObstruction.class, ElspethSunsNemesis.class, Ichthyomorphosis.class})
    void losingAllAbilitiesRemovesLoyaltyAbilityTax() {
        Permanent eidolon = harness.addToBattlefieldAndReturn(player1, new EidolonOfObstruction());
        Permanent elspeth = harness.addToBattlefieldAndReturn(player2, new ElspethSunsNemesis());
        elspeth.setCounterCount(CounterType.LOYALTY, 5);
        prepareTurn(player2);
        harness.setHand(player2, List.of(new Ichthyomorphosis()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player2, 0, eidolon.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasLostAllAbilities(gd, eidolon)).isTrue();
        harness.activateAbility(player2, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(25);
    }

    private void prepareTurn(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
