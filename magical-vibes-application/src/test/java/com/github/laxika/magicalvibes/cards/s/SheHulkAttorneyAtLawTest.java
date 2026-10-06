package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SheHulkAttorneyAtLaw.class, GrizzlyBears.class})
class SheHulkAttorneyAtLawTest extends BaseCardTest {

    @Test
    @DisplayName("Entry-turn power-up adds a counter and doubles controlled creatures' counters")
    void entryTurnPowerUpAddsAndDoublesCounters() {
        Permanent sheHulk = harness.enterBattlefieldAndReturn(player1, new SheHulkAttorneyAtLaw());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        opposingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sheHulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power-up can be activated only once")
    void powerUpCanBeActivatedOnlyOnce() {
        addCreatureReady(player1, new SheHulkAttorneyAtLaw());
        harness.addMana(player1, ManaColor.COLORLESS, 12);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Entry-turn reduction removes both generic and hybrid mana requirements")
    void entryTurnPowerUpCostsFourGenericMana() {
        Permanent sheHulk = harness.enterBattlefieldAndReturn(player1, new SheHulkAttorneyAtLaw());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sheHulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Outside the entry turn the full cost can be paid with white mana")
    void fullCostAcceptsWhiteMana() {
        Permanent sheHulk = addCreatureReady(player1, new SheHulkAttorneyAtLaw());
        sheHulk.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sheHulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
    }

    @Test
    @DisplayName("Doubling does not add counters to creatures with none or double other counter types")
    void doublingOnlyAddsExistingPlusOneCounters() {
        Permanent sheHulk = harness.enterBattlefieldAndReturn(player1, new SheHulkAttorneyAtLaw());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ownCreature.setCounterCount(CounterType.CHARGE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sheHulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownCreature.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("The once-only restriction applies before the first activation resolves")
    void cannotActivateAgainWhileAbilityIsOnStack() {
        Permanent sheHulk = harness.enterBattlefieldAndReturn(player1, new SheHulkAttorneyAtLaw());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
        harness.passBothPriorities();
        assertThat(sheHulk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
