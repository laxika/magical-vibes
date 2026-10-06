package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DrownedRusalka;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkarrganFirebird.class, DrownedRusalka.class})
class SkarrganFirebirdTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 3: enters with three +1/+1 counters when an opponent was dealt damage")
    void bloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castFirebird();

        assertThat(findPermanent(player1, "Skarrgan Firebird")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bloodthirst 3: enters without counters when no opponent was dealt damage")
    void bloodthirstDoesNotApply() {
        castFirebird();

        assertThat(findPermanent(player1, "Skarrgan Firebird")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The graveyard ability cannot be activated before an opponent was dealt damage")
    void cannotActivateBeforeOpponentWasDealtDamage() {
        harness.setGraveyard(player1, List.of(new SkarrganFirebird()));
        addReturnAbilityMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The graveyard ability returns Skarrgan Firebird to its owner's hand after an opponent was dealt damage")
    void returnsFromGraveyardAfterOpponentWasDealtDamage() {
        harness.setGraveyard(player1, List.of(new SkarrganFirebird()));
        addReturnAbilityMana();
        gd.recordDamageToPlayer(player2.getId(), 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Skarrgan Firebird");
        harness.assertNotInGraveyard(player1, "Skarrgan Firebird");
    }

    @Test
    @DisplayName("The graveyard ability returns only the activating Firebird when multiple copies are present")
    void returnsOnlyTheActivatingFirebird() {
        SkarrganFirebird activatingFirebird = new SkarrganFirebird();
        SkarrganFirebird otherFirebird = new SkarrganFirebird();
        harness.setGraveyard(player1, List.of(activatingFirebird, otherFirebird));
        addReturnAbilityMana();
        gd.recordDamageToPlayer(player2.getId(), 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof SkarrganFirebird)
                .containsExactly(activatingFirebird);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherFirebird);
    }

    @Test
    @DisplayName("The graveyard ability ignores damage dealt to its controller")
    void cannotActivateAfterControllerWasDealtDamage() {
        harness.setGraveyard(player1, List.of(new SkarrganFirebird()));
        addReturnAbilityMana();
        gd.recordDamageToPlayer(player1.getId(), 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Bloodthirst ignores damage dealt only to its controller")
    void bloodthirstIgnoresControllerDamage() {
        gd.recordDamageToPlayer(player1.getId(), 3);
        castFirebird();

        assertThat(findPermanent(player1, "Skarrgan Firebird")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Opponent life loss without damage does not enable either ability")
    void lifeLossDoesNotEnableBloodthirstOrReturn() {
        harness.setLife(player2, 17);
        castFirebird();

        assertThat(findPermanent(player1, "Skarrgan Firebird")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.setGraveyard(player1, List.of(new SkarrganFirebird()));
        addReturnAbilityMana();
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Bloodthirst checks damage as the creature enters, rather than when it is cast")
    void bloodthirstChecksDamageAtEntry() {
        harness.castFromHand(player1, new SkarrganFirebird(), "{4}{R}{R}");
        gd.recordDamageToPlayer(player2.getId(), 5);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Skarrgan Firebird")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bloodthirst applies immediately when Firebird enters without being cast")
    void bloodthirstAppliesWithoutCasting() {
        gd.recordDamageToPlayer(player2.getId(), 1);

        var firebird = harness.enterBattlefieldAndReturn(player1, new SkarrganFirebird());

        assertThat(firebird.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returning Firebird requires three red mana, not two red and one colorless")
    void returnRequiresThreeRedMana() {
        harness.setGraveyard(player1, List.of(new SkarrganFirebird()));
        gd.recordDamageToPlayer(player2.getId(), 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Skarrgan Firebird");
        harness.assertNotInHand(player1, "Skarrgan Firebird");
    }

    @Test
    @DisplayName("An older activation cannot return Firebird after it leaves and reenters the graveyard")
    void olderActivationCannotReturnNewGraveyardObject() {
        SkarrganFirebird firebird = new SkarrganFirebird();
        DrownedRusalka drawn = new DrownedRusalka();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(firebird));
        harness.setLibrary(player1, List.of(drawn));
        harness.addToBattlefield(player1, new DrownedRusalka());
        gd.recordDamageToPlayer(player2.getId(), 1);
        harness.addMana(player1, ManaColor.RED, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firebird);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firebird);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firebird);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    private void castFirebird() {
        harness.castFromHand(player1, new SkarrganFirebird(), "{4}{R}{R}");
        resolveAllTriggers();
    }

    private void addReturnAbilityMana() {
        harness.addMana(player1, ManaColor.RED, 3);
    }
}
