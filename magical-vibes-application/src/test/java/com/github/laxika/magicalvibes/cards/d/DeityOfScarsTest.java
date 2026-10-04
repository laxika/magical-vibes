package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({DeityOfScars.class})
class DeityOfScarsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with two -1/-1 counters (effectively 5/5)")
    void entersWithTwoMinusCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DeityOfScars()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent deity = deity(player1);
        assertThat(deity.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(deity.getEffectivePower()).isEqualTo(5);
        assertThat(deity.getEffectiveToughness()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activating the ability grants a regeneration shield and removes a -1/-1 counter")
    void abilityGrantsShieldAndRemovesCounter() {
        Permanent deity = addReadyDeity(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(deity.getRegenerationShield()).isEqualTo(1);
        assertThat(deity.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(deity.getEffectivePower()).isEqualTo(6);
        assertThat(deity.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Hybrid cost can be paid with green mana")
    void abilityPaidWithGreenMana() {
        Permanent deity = addReadyDeity(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(deity.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability when no -1/-1 counters remain")
    void cannotActivateWithoutCounters() {
        Permanent deity = addReadyDeity(player1);
        deity.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyDeity(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Both counters can be spent before either regeneration ability resolves")
    void canActivateTwiceBeforeResolution() {
        Permanent deity = addReadyDeity(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(deity.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(deity.getRegenerationShield()).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(deity.getRegenerationShield()).isEqualTo(2);
        assertThat(deity.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("The ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent deity = addReadyDeity(player1);
        deity.setSummoningSick(true);
        deity.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(deity.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(deity.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration prevents lethal-damage destruction, taps the creature and clears damage")
    void regeneratesFromLethalDamage() {
        Permanent deity = addReadyDeity(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        deity.addMarkedDamage(null, 6);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(deity);
        assertThat(deity.isTapped()).isTrue();
        assertThat(deity.getMarkedDamage()).isZero();
        assertThat(deity.getRegenerationShield()).isZero();
        assertThat(deity.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    private Permanent deity(Player player) {
        return findPermanent(player, "Deity of Scars");
    }

    private Permanent addReadyDeity(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new DeityOfScars());
        perm.setSummoningSick(false);
        perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        return perm;
    }
}
