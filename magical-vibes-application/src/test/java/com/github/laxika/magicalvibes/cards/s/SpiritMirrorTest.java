package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpiritMirrorTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger creates a 2/2 Reflection token")
    void upkeepCreatesReflection() {
        addMirror(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Reflection");
        assertThat(token).isNotNull();
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Trigger does not fire while a Reflection token is on the battlefield")
    void doesNotTriggerWithReflectionPresent() {
        addMirror(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Reflection")).isEqualTo(1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Reflection")).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's Reflection token also stops the trigger")
    void opponentReflectionStopsTrigger() {
        addMirror(player1);
        Permanent mirror2 = addMirror(player2);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(countPermanents(player2, "Reflection")).isEqualTo(1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Reflection")).isZero();
        assertThat(mirror2).isNotNull();
    }

    @Test
    @DisplayName("{0} ability destroys a target Reflection")
    void abilityDestroysReflection() {
        addMirror(player1);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Reflection");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, token.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Reflection")).isZero();
    }

    @Test
    @DisplayName("Destroying the Reflection lets the next upkeep make a new one")
    void newTokenAfterDestruction() {
        addMirror(player1);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Reflection");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, token.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Reflection")).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-Reflection creature")
    void cannotTargetNonReflection() {
        addMirror(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addMirror(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SpiritMirror());
    }
}
