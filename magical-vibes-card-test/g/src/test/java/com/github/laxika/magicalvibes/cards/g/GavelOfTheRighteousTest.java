package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GavelOfTheRighteous.class, GrizzlyBears.class})
class GavelOfTheRighteousTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, Gavel gets a charge counter and scales its boost")
    void chargesAtBeginningOfCombat() {
        Permanent gavel = addGavelReady();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        gavel.setAttachedTo(bear.getId());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(gavel.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
    }

    @Test
    @DisplayName("Four or more counters grant double strike to the equipped creature")
    void fourCountersGrantDoubleStrike() {
        Permanent gavel = addGavelReady();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        gavel.setAttachedTo(bear.getId());
        gavel.setCounterCount(CounterType.CHARGE, 4);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.DOUBLE_STRIKE)).isTrue();

        gavel.setCounterCount(CounterType.CHARGE, 3);

        assertThat(gqs.hasKeyword(gd, bear, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Equip can be paid with three mana")
    void equipsForMana() {
        Permanent gavel = addGavelReady();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(gavel.getAttachedTo()).isEqualTo(bear.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Equip can remove any counter instead of paying mana")
    void equipsByRemovingAnyCounter() {
        Permanent gavel = addGavelReady();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        gavel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, 1, null, bear.getId());
        harness.passBothPriorities();

        assertThat(gavel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gavel.getAttachedTo()).isEqualTo(bear.getId());
    }

    @Test
    void doesNotChargeDuringOpponentsCombat() {
        Permanent gavel = addGavelReady();

        advanceToBeginningOfCombat(player2);
        harness.passBothPriorities();

        assertThat(gavel.getTotalCounterCount()).isZero();
    }

    @Test
    void countsMixedCounterTypesForBothStaticAbilities() {
        Permanent gavel = addGavelReady();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        gavel.setAttachedTo(bear.getId());
        gavel.setCounterCount(CounterType.CHARGE, 2);
        gavel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void removingCounterImmediatelyReducesBonusBeforeEquipResolves() {
        Permanent gavel = addGavelReady();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherBear = addCreatureReady(player1, new GrizzlyBears());
        gavel.setAttachedTo(bear.getId());
        gavel.setCounterCount(CounterType.CHARGE, 4);

        harness.activateAbility(player1, 0, 1, null, otherBear.getId());

        assertThat(gavel.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(gavel.getAttachedTo()).isEqualTo(bear.getId());
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.DOUBLE_STRIKE)).isFalse();
        harness.passBothPriorities();

        assertThat(gavel.getAttachedTo()).isEqualTo(otherBear.getId());
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, otherBear)).isEqualTo(5);
    }

    @Test
    void cannotEquipByRemovingCounterWhenThereAreNone() {
        addGavelReady();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void neitherEquipPaymentCanTargetOpponentsCreature() {
        Permanent gavel = addGavelReady();
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        gavel.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 3);

        for (int abilityIndex = 0; abilityIndex < 2; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, index, null, bear.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
        assertThat(gavel.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gavel.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    void bothEquipPaymentsRequireSorceryTiming() {
        Permanent gavel = addGavelReady();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        gavel.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        for (int abilityIndex = 0; abilityIndex < 2; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, index, null, bear.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
        assertThat(gavel.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mixedCounterCostRequiresPlayersChoiceInsteadOfAutomaticRemoval() {
        Permanent gavel = addGavelReady();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        gavel.setCounterCount(CounterType.CHARGE, 1);
        gavel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, 1, null, bear.getId());

        assertThat(gavel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gavel.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    private Permanent addGavelReady() {
        Permanent gavel = harness.addToBattlefieldAndReturn(player1, new GavelOfTheRighteous());
        gavel.setSummoningSick(false);
        return gavel;
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
