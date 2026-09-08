package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static com.github.laxika.magicalvibes.model.CounterType.CREDIT;

@CardUsed(IcatianMoneychanger.class)
class IcatianMoneychangerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three credit counters and deals 3 damage to its controller")
    void entersWithCreditCountersAndDealsDamage() {
        harness.castFromHand(player1, new IcatianMoneychanger(), "{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent moneychanger = findPermanent(player1, "Icatian Moneychanger");
        assertThat(moneychanger.getCounterCount(CREDIT)).isEqualTo(3);
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Upkeep trigger adds a credit counter")
    void upkeepTriggerAddsCreditCounter() {
        Permanent moneychanger = addReadyMoneychanger();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(moneychanger.getCounterCount(CREDIT)).isEqualTo(1);
    }

    @Test
    @DisplayName("Upkeep trigger does not add a credit counter during an opponent's upkeep")
    void upkeepTriggerOnlyAppliesDuringYourUpkeep() {
        Permanent moneychanger = addReadyMoneychanger();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(moneychanger.getCounterCount(CREDIT)).isZero();
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
        harness.assertLife(player1, 24);
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

    @Test
    @DisplayName("Sacrifice ability cannot be activated during an opponent's upkeep")
    void sacrificeAbilityCannotBeActivatedDuringOpponentsUpkeep() {
        Permanent moneychanger = addReadyMoneychanger();
        moneychanger.setCounterCount(CREDIT, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    private Permanent addReadyMoneychanger() {
        return addCreatureReady(player1, new IcatianMoneychanger());
    }
}
