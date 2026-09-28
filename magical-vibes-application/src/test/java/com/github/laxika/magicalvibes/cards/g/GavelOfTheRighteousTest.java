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

    private Permanent addGavelReady() {
        Permanent gavel = harness.addToBattlefieldAndReturn(player1, new GavelOfTheRighteous());
        gavel.setSummoningSick(false);
        return gavel;
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
