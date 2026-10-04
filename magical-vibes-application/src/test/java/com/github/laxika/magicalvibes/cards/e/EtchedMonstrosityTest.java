package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EtchedMonstrosity.class})
class EtchedMonstrosityTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with five -1/-1 counters (10/10 becomes 5/5)")
    void entersWithFiveMinusCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new EtchedMonstrosity()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        Permanent monstrosity = findMonstrosity(player1);

        assertThat(monstrosity.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(5);
        assertThat(monstrosity.getEffectivePower()).isEqualTo(5);
        assertThat(monstrosity.getEffectiveToughness()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activated ability makes target player draw three cards")
    void abilityTargetPlayerDrawsThreeCards() {
        addReadyMonstrosity(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addWUBRGMana(player1);

        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handSizeBefore + 3);
    }

    @Test
    @DisplayName("Activated ability can target self to draw three cards")
    void abilityCanTargetSelf() {
        addReadyMonstrosity(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addWUBRGMana(player1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 3);
    }

    @Test
    @DisplayName("Activated ability removes all five -1/-1 counters as cost")
    void abilityRemovesFiveCounters() {
        Permanent monstrosity = addReadyMonstrosity(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addWUBRGMana(player1);

        harness.activateAbility(player1, 0, null, player1.getId());
        assertThat(monstrosity.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
        assertThat(monstrosity.getEffectivePower()).isEqualTo(10);
        assertThat(monstrosity.getEffectiveToughness()).isEqualTo(10);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Cannot activate ability when fewer than five counters remain")
    void cannotActivateWithoutEnoughCounters() {
        Permanent monstrosity = addReadyMonstrosity(player1);
        monstrosity.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addWUBRGMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("Cannot activate ability when zero counters remain")
    void cannotActivateWithZeroCounters() {
        Permanent monstrosity = addReadyMonstrosity(player1);
        monstrosity.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addWUBRGMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("Cannot activate ability with +1/+1 counters instead of -1/-1 counters")
    void cannotActivateWithPlusCounters() {
        Permanent monstrosity = addReadyMonstrosity(player1);
        monstrosity.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        monstrosity.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addWUBRGMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("Enters with counters immediately when put onto the battlefield without casting")
    void entersWithCountersWithoutCasting() {
        Permanent monstrosity = harness.enterBattlefieldAndReturn(player1, new EtchedMonstrosity());

        assertThat(monstrosity.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped summoning-sick Monstrosity can activate and removes exactly five counters")
    void canActivateWhileTappedAndSummoningSickWithExtraCounters() {
        Permanent monstrosity = harness.addToBattlefieldAndReturn(player1, new EtchedMonstrosity());
        monstrosity.setSummoningSick(true);
        monstrosity.setTapped(true);
        monstrosity.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addWUBRGMana(player1);
        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(monstrosity.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(monstrosity.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore + 3);
    }

    @Test
    @DisplayName("Colorless mana cannot replace the five required colors")
    void cannotActivateWithOnlyColorlessMana() {
        Permanent monstrosity = addReadyMonstrosity(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(monstrosity.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyMonstrosity(Player player) {
        Permanent perm = addCreatureReady(player, new EtchedMonstrosity());
        perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 5);
        return perm;
    }

    private Permanent findMonstrosity(Player player) {
        return findPermanent(player, "Etched Monstrosity");
    }

    private void addWUBRGMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }
}
