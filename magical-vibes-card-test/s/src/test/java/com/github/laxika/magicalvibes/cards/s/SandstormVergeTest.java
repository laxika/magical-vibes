package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SandstormVerge.class, SterlingHound.class, Forest.class})
class SandstormVergeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping produces one colorless mana")
    void tapsForColorlessMana() {
        Permanent land = addReadyLand();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sorcery-speed ability makes the target unable to block this turn")
    void targetCannotBlockThisTurn() {
        Permanent land = addReadyLand();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SterlingHound());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The blocking restriction wears off at cleanup")
    void blockingRestrictionWearsOffAtCleanup() {
        addReadyLand();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SterlingHound());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The second ability can only be activated at sorcery speed")
    void secondAbilityRequiresSorcerySpeed() {
        addReadyLand();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SterlingHound());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("The second ability cannot target a noncreature permanent")
    void secondAbilityRejectsNoncreatureTarget() {
        addReadyLand();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability can target a creature you control in the postcombat main phase")
    void canTargetOwnCreatureInPostcombatMainPhase() {
        addReadyLand();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SterlingHound());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        assertThat(target.isCantBlockThisTurn()).isFalse();
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The second ability cannot be activated outside a main phase")
    void secondAbilityRequiresMainPhase() {
        Permanent land = addReadyLand();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SterlingHound());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The second ability cannot be activated with another ability on the stack")
    void secondAbilityRequiresEmptyStack() {
        addReadyLand();
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new SandstormVerge());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SterlingHound());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, 1, null, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(secondLand.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("The second ability requires three mana")
    void secondAbilityRequiresEnoughMana() {
        Permanent land = addReadyLand();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SterlingHound());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(land.isTapped()).isFalse();
        assertThat(target.isCantBlockThisTurn()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SandstormVerge());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return land;
    }
}
