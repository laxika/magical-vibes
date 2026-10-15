package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({GoldenUrn.class})
class GoldenUrnTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger may add a charge counter to Golden Urn")
    void upkeepTriggerMayAddChargeCounter() {
        Permanent urn = addReadyUrn(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, true); // inner resolves inline

        assertThat(urn.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Upkeep trigger can decline to add a charge counter")
    void upkeepTriggerCanDeclineToAddCounter() {
        Permanent urn = addReadyUrn(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(urn.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Multiple upkeep triggers accumulate charge counters")
    void multipleUpkeepTriggersAccumulateCounters() {
        Permanent urn = addReadyUrn(player1);

        // First upkeep - add counter
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, true); // inner resolves inline

        assertThat(urn.getCounterCount(CounterType.CHARGE)).isEqualTo(1);

        // Second upkeep - add counter
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, true); // inner resolves inline

        assertThat(urn.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing Golden Urn gains life equal to charge counters")
    void sacrificeSelfGainsLifeEqualToChargeCounters() {
        Permanent urn = addReadyUrn(player1);
        urn.setCounterCount(CounterType.CHARGE, 3);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Golden Urn should be in graveyard
        harness.assertNotOnBattlefield(player1, "Golden Urn");
        harness.assertInGraveyard(player1, "Golden Urn");

        // Player should have gained 3 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Sacrificing Golden Urn with 0 counters gains 0 life")
    void sacrificeSelfWithZeroCountersGainsNoLife() {
        Permanent urn = addReadyUrn(player1);
        // No charge counters

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Golden Urn should be in graveyard
        harness.assertNotOnBattlefield(player1, "Golden Urn");

        // Player should not have gained any life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Activated ability requires tap")
    void activatedAbilityRequiresTap() {
        Permanent urn = addReadyUrn(player1);
        urn.tap();

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, null)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing Golden Urn with accumulated counters gains correct life")
    void sacrificingWithAccumulatedCounters() {
        Permanent urn = addReadyUrn(player1);

        // Add counters through multiple uptaps (manually for testing)
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, true); // inner resolves inline

        assertThat(urn.getCounterCount(CounterType.CHARGE)).isEqualTo(1);

        // Untap for activation
        urn.untap();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Should have gained 1 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Golden Urn goes to graveyard immediately on sacrifice")
    void goldenUrnGoesToGraveyardOnSacrifice() {
        Permanent urn = addReadyUrn(player1);
        urn.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, null, null);

        // Should be in graveyard immediately (sacrifice is a cost)
        harness.assertNotOnBattlefield(player1, "Golden Urn");
        harness.assertInGraveyard(player1, "Golden Urn");
    }

    @Test
    @DisplayName("Golden Urn does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent urn = addReadyUrn(player1);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(urn.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Sacrificing in response to upkeep gains life before the counter can be added")
    void sacrificeInResponseToUpkeep() {
        Permanent urn = addReadyUrn(player1);
        urn.setCounterCount(CounterType.CHARGE, 2);
        advanceToUpkeep(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Golden Urn");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Golden Urn");
        harness.assertLife(player1, 22);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A newly controlled noncreature Golden Urn can activate and counts only charge counters")
    void newlyControlledUrnCountsOnlyChargeCounters() {
        Permanent urn = harness.addToBattlefieldAndReturn(player1, new GoldenUrn());
        urn.setSummoningSick(true);
        urn.setCounterCount(CounterType.CHARGE, 2);
        urn.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Golden Urn");
    }

    private Permanent addReadyUrn(Player player) {
        return addCreatureReady(player, new GoldenUrn());
    }
}
