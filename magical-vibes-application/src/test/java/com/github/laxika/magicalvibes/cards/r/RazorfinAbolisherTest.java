package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DuskdaleWurm;
import com.github.laxika.magicalvibes.cards.f.FloodedGrove;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RazorfinAbolisher.class, DuskdaleWurm.class, FloodedGrove.class})
class RazorfinAbolisherTest extends BaseCardTest {

    // ===== Resolving =====

    @Test
    @DisplayName("Ability returns target creature with a counter to its owner's hand")
    void abilityReturnsCreatureWithCounter() {
        Permanent abolisher = addReadyAbolisher(player1);
        Permanent target = addCreatureWithCounter(player2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(abolisher.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Duskdale Wurm");
        harness.assertInHand(player2, "Duskdale Wurm");
    }

    // ===== Illegal targets =====

    @Test
    @DisplayName("Ability cannot target a creature without a counter")
    void cannotTargetCreatureWithoutCounter() {
        addReadyAbolisher(player1);
        Permanent target = addCreatureReady(player2, new DuskdaleWurm());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Cannot activate =====

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent abolisher = addReadyAbolisher(player1);
        abolisher.tap();
        Permanent target = addCreatureWithCounter(player2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Ability returns a creature with a non-plus-one counter under its controller's control")
    void abilityReturnsOwnCreatureWithAnyCounter() {
        addReadyAbolisher(player1);
        Permanent target = addCreatureWithCounter(player1, CounterType.MINUS_ONE_MINUS_ONE);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Duskdale Wurm");
        harness.assertInHand(player1, "Duskdale Wurm");
    }

    @Test
    @DisplayName("Ability cannot target a noncreature permanent with a counter")
    void cannotTargetNonCreatureWithCounter() {
        addReadyAbolisher(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FloodedGrove());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Razorfin Abolisher").isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Flooded Grove");
    }

    @Test
    @DisplayName("Ability does not return a target whose counter is removed before resolution")
    void doesNotReturnTargetWithoutCounterAtResolution() {
        addReadyAbolisher(player1);
        Permanent target = addCreatureWithCounter(player2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Duskdale Wurm");
        harness.assertNotInHand(player2, "Duskdale Wurm");
    }

    private Permanent addReadyAbolisher(Player player) {
        return addCreatureReady(player, new RazorfinAbolisher());
    }

    private Permanent addCreatureWithCounter(Player player) {
        return addCreatureWithCounter(player, CounterType.PLUS_ONE_PLUS_ONE);
    }

    private Permanent addCreatureWithCounter(Player player, CounterType counterType) {
        Permanent perm = addCreatureReady(player, new DuskdaleWurm());
        perm.setCounterCount(counterType, 1);
        return perm;
    }
}
