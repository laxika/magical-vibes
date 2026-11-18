package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(WalkingWall.class)
class WalkingWallTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack without activating the ability (defender)")
    void cannotAttackWithDefender() {
        addCreatureReady(player1, new WalkingWall());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Ability gives +3/-1 and lets the wall attack this turn")
    void abilityBoostsAndAllowsAttack() {
        Permanent wall = addCreatureReady(player1, new WalkingWall());
        harness.addToBattlefield(player2, new WalkingWall());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(5);

        declareAttackers(List.of(0));

        assertThat(wall.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate more than once each turn")
    void onlyOncePerTurn() {
        Permanent wall = addCreatureReady(player1, new WalkingWall());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(PermanentGrants.powerBoostOf(gd, wall)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost and attack permission wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent wall = addCreatureReady(player1, new WalkingWall());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(PermanentGrants.powerBoostOf(gd, wall)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, wall)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, wall)).isEqualTo(0);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new WalkingWall());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Activation limit applies while the first activation is still on the stack")
    void cannotActivateAgainBeforeResolution() {
        Permanent wall = addCreatureReady(player1, new WalkingWall());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);

        assertThat(PermanentGrants.powerBoostOf(gd, wall)).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(5);
    }

    @Test
    @DisplayName("Each Walking Wall has its own activation limit")
    void separateCopiesCanEachActivate() {
        Permanent first = addCreatureReady(player1, new WalkingWall());
        Permanent second = addCreatureReady(player1, new WalkingWall());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isEqualTo(3);
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(3);
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isEqualTo(-1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isEqualTo(-1);
    }

    @Test
    @DisplayName("The ability can be activated again on the opponent's next turn")
    void activationLimitResetsOnNextTurn() {
        Permanent wall = addCreatureReady(player1, new WalkingWall());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(PermanentGrants.powerBoostOf(gd, wall)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, wall)).isZero();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(5);
    }

    @Test
    @DisplayName("A summoning-sick wall can activate but still cannot attack")
    void attackPermissionDoesNotGrantHaste() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WalkingWall());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, wall)).isEqualTo(3);
        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }
}
