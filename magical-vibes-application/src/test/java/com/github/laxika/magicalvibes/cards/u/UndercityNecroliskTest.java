package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.BartizanBats;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UndercityNecrolisk.class, BartizanBats.class})
class UndercityNecroliskTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts a counter on Undercity Necrolisk and grants menace")
    void sacrificingAnotherCreaturePutsCounterAndGrantsMenace() {
        Permanent necrolisk = addCreatureReady(player1, new UndercityNecrolisk());
        harness.addToBattlefield(player1, new BartizanBats());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(necrolisk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, necrolisk, Keyword.MENACE)).isTrue();
        harness.assertInGraveyard(player1, "Bartizan Bats");
        harness.assertOnBattlefield(player1, "Undercity Necrolisk");
    }

    @Test
    @DisplayName("Menace wears off at the end of the turn while the counter remains")
    void menaceWearsOffAtEndOfTurn() {
        Permanent necrolisk = addCreatureReady(player1, new UndercityNecrolisk());
        harness.addToBattlefield(player1, new BartizanBats());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(necrolisk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, necrolisk, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("The activated ability cannot sacrifice Undercity Necrolisk itself")
    void activatedAbilityRequiresAnotherCreature() {
        addCreatureReady(player1, new UndercityNecrolisk());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canActivateWhileTappedAndSummoningSickAndPaysSacrificeBeforeResolution() {
        Permanent necrolisk = harness.addToBattlefieldAndReturn(player1, new UndercityNecrolisk());
        necrolisk.setSummoningSick(true);
        necrolisk.tap();
        harness.addToBattlefield(player1, new BartizanBats());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Bartizan Bats");
        assertThat(necrolisk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, necrolisk, Keyword.MENACE)).isFalse();

        harness.passBothPriorities();

        assertThat(necrolisk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, necrolisk, Keyword.MENACE)).isTrue();
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        addCreatureReady(player1, new UndercityNecrolisk());
        harness.addToBattlefield(player1, new BartizanBats());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Bartizan Bats");
    }

    @Test
    void cannotActivateDuringOpponentsTurn() {
        addCreatureReady(player1, new UndercityNecrolisk());
        harness.addToBattlefield(player1, new BartizanBats());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Bartizan Bats");
    }

    @Test
    void cannotActivateWithNonemptyStack() {
        Permanent necrolisk = addCreatureReady(player1, new UndercityNecrolisk());
        harness.addToBattlefield(player1, new BartizanBats());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.addToBattlefield(player1, new BartizanBats());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Bartizan Bats");

        harness.passBothPriorities();
        assertThat(necrolisk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotSacrificeOpponentsCreature() {
        addCreatureReady(player1, new UndercityNecrolisk());
        harness.addToBattlefield(player2, new BartizanBats());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Bartizan Bats");
    }

    @Test
    void canActivateAgainInPostcombatMainPhase() {
        Permanent necrolisk = addCreatureReady(player1, new UndercityNecrolisk());
        harness.addToBattlefield(player1, new BartizanBats());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addToBattlefield(player1, new BartizanBats());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(necrolisk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, necrolisk, Keyword.MENACE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Bartizan Bats")).hasSize(2);
    }

    @Test
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new UndercityNecrolisk());
        harness.addToBattlefield(player1, new BartizanBats());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Bartizan Bats");
    }
}
