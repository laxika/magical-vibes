package com.github.laxika.magicalvibes.cards.a;

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

@CardUsed({AbominationTerrifyingTitan.class, GrizzlyBears.class})
class AbominationTerrifyingTitanTest extends BaseCardTest {

    @Test
    @DisplayName("Entry-turn power-up puts a counter on Abomination and makes him fight an opposing creature")
    void powerUpIsDiscountedAndFightsOpponentCreature() {
        Permanent abomination = harness.enterBattlefieldAndReturn(player1, new AbominationTerrifyingTitan());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(abomination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Power-up may be activated without choosing a creature")
    void powerUpMayChooseNoTarget() {
        Permanent abomination = harness.enterBattlefieldAndReturn(player1, new AbominationTerrifyingTitan());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(abomination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power-up costs its full activation cost after the entry turn")
    void powerUpIsNotDiscountedAfterEntryTurn() {
        Permanent abomination = addReadyAbomination();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(abomination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power-up cannot target a creature you control")
    void powerUpCannotTargetOwnCreature() {
        addReadyAbomination();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    @Test
    @DisplayName("Power-up can be activated only once")
    void powerUpCanBeActivatedOnlyOnce() {
        addReadyAbomination();
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    private Permanent addReadyAbomination() {
        return addCreatureReady(player1, new AbominationTerrifyingTitan());
    }

    @Test
    @DisplayName("Entry-turn reduction includes the hybrid symbol in Abomination's mana cost")
    void entryTurnPowerUpCostsTwoGenericAndOneRed() {
        Permanent abomination = harness.enterBattlefieldAndReturn(player1, new AbominationTerrifyingTitan());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(abomination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Entry-turn power-up can use green for its remaining hybrid symbol")
    void entryTurnPowerUpCostsTwoGenericAndOneGreen() {
        Permanent abomination = harness.enterBattlefieldAndReturn(player1, new AbominationTerrifyingTitan());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(abomination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The entry-turn cost cannot be paid after Abomination's entry turn")
    void discountedManaIsInsufficientAfterEntryTurn() {
        addReadyAbomination();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The counter is placed before fight damage and both creatures deal damage")
    void counterLetsAbominationSurviveFightingAnotherTitan() {
        Permanent abomination = addReadyAbomination();
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new AbominationTerrifyingTitan());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, opponent.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Abomination, Terrifying Titan");
        harness.assertInGraveyard(player2, "Abomination, Terrifying Titan");
        assertThat(abomination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(abomination.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Losing the only chosen target prevents the entire power-up from resolving")
    void missingTargetPreventsCounterPlacement() {
        Permanent abomination = addReadyAbomination();
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, opponent.getId());
        gd.playerBattlefields.get(player2.getId()).remove(opponent);
        harness.passBothPriorities();

        assertThat(abomination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(abomination.getMarkedDamage()).isZero();
    }
}
