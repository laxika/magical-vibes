package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeityOfScarsTest extends BaseCardTest {

    // ===== ETB: enters with two -1/-1 counters =====

    @Test
    @DisplayName("Enters the battlefield with two -1/-1 counters (effectively 5/5)")
    void entersWithTwoMinusCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DeityOfScars()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (enters battlefield)
        harness.passBothPriorities(); // resolve ETB effect (puts -1/-1 counters)

        Permanent deity = deity(player1);
        assertThat(deity.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(deity.getEffectivePower()).isEqualTo(5);
        assertThat(deity.getEffectiveToughness()).isEqualTo(5);
    }

    // ===== Activated ability: regenerate =====

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

    // ===== Helpers =====

    private Permanent deity(Player player) {
        return harness.getGameData().playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Deity of Scars"))
                .findFirst().orElseThrow();
    }

    private Permanent addReadyDeity(Player player) {
        Permanent perm = new Permanent(new DeityOfScars());
        perm.setSummoningSick(false);
        perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
