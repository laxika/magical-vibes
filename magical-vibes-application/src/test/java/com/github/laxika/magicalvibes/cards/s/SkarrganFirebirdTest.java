package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SkarrganFirebird.class)
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
    @DisplayName("The graveyard ability ignores damage dealt to its controller")
    void cannotActivateAfterControllerWasDealtDamage() {
        harness.setGraveyard(player1, List.of(new SkarrganFirebird()));
        addReturnAbilityMana();
        gd.recordDamageToPlayer(player1.getId(), 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castFirebird() {
        harness.setHand(player1, List.of(new SkarrganFirebird()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private void addReturnAbilityMana() {
        harness.addMana(player1, ManaColor.RED, 3);
    }
}
