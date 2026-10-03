package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CoalitionRelic.class)
class CoalitionRelicTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one mana of the chosen color")
    void tappingAddsAnyColorMana() {
        Permanent relic = addRelic(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(relic.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping puts a charge counter on the relic")
    void tappingAddsChargeCounter() {
        Permanent relic = addRelic(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(relic.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(relic.isTapped()).isTrue();
    }

    @Test
    @DisplayName("First main phase removes charge counters and adds one mana per counter")
    void firstMainPhaseConvertsChargeCountersToMana() {
        Permanent relic = addRelic(player1);
        relic.setCounterCount(CounterType.CHARGE, 2);

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(relic.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("First main phase trigger resolves with no mana when there are no charge counters")
    void firstMainPhaseWithNoChargeCountersAddsNoMana() {
        addRelic(player1);

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("First main phase trigger only fires during the controller's first main phase")
    void firstMainPhaseTriggerOnlyFiresOnControllerTurn() {
        Permanent relic = addRelic(player1);
        relic.setCounterCount(CounterType.CHARGE, 2);

        advanceToPrecombatMain(player2);
        harness.passBothPriorities();

        assertThat(relic.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A charge counter added in response to the main phase trigger is converted to mana")
    void chargeInResponseIsCountedAtResolution() {
        Permanent relic = addRelic(player1);
        advanceToPrecombatMain(player1);

        assertThat(gd.stack).hasSize(1);
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(relic.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        assertThat(relic.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(relic.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Conversion removes only charge counters and permits choosing the same color repeatedly")
    void conversionPreservesOtherCountersAndAllowsRepeatedColor() {
        Permanent relic = addRelic(player1);
        relic.setCounterCount(CounterType.CHARGE, 2);
        relic.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");
        harness.handleListChoice(player1, "WHITE");

        assertThat(relic.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(relic.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
        assertThat(relic.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addRelic(Player player) {
        return harness.addToBattlefieldAndReturn(player, new CoalitionRelic());
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}
