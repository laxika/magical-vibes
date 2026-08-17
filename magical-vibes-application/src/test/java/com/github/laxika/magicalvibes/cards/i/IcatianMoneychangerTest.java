package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static com.github.laxika.magicalvibes.model.CounterType.CREDIT;

class IcatianMoneychangerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three credit counters and deals 3 damage to its controller")
    void entersWithCreditCountersAndDealsDamage() {
        harness.setHand(player1, List.of(new IcatianMoneychanger()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent moneychanger = findPermanent(player1, "Icatian Moneychanger");
        assertThat(moneychanger.getCounterCount(CREDIT)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Upkeep trigger adds a credit counter")
    void upkeepTriggerAddsCreditCounter() {
        Permanent moneychanger = addReadyMoneychanger();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(moneychanger.getCounterCount(CREDIT)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing during upkeep gains life equal to its credit counters")
    void sacrificeGainsLifeEqualToCreditCounters() {
        Permanent moneychanger = addReadyMoneychanger();
        moneychanger.setCounterCount(CREDIT, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Icatian Moneychanger");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("Sacrifice ability cannot be activated outside its controller's upkeep")
    void sacrificeAbilityRequiresYourUpkeep() {
        Permanent moneychanger = addReadyMoneychanger();
        moneychanger.setCounterCount(CREDIT, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    private Permanent addReadyMoneychanger() {
        Permanent moneychanger = harness.addToBattlefieldAndReturn(player1, new IcatianMoneychanger());
        moneychanger.setSummoningSick(false);
        return moneychanger;
    }
}
