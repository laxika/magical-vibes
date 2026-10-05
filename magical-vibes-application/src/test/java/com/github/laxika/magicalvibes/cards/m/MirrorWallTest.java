package com.github.laxika.magicalvibes.cards.m;

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

@CardUsed({MirrorWall.class})
class MirrorWallTest extends BaseCardTest {

    private Permanent addWallReady() {
        return addCreatureReady(player1, new MirrorWall());
    }

    @Test
    @DisplayName("Cannot attack without activating the ability")
    void cannotAttackWithDefender() {
        addWallReady();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Ability lets the wall attack this turn")
    void abilityAllowsAttack() {
        Permanent wall = addWallReady();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(wall.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability does not tap the wall")
    void activationDoesNotTapWall() {
        Permanent wall = addWallReady();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wall.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ability does not bypass summoning sickness")
    void abilityDoesNotBypassSummoningSickness() {
        harness.addToBattlefieldAndReturn(player1, new MirrorWall());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Attack permission wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        addWallReady();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Cannot activate the ability without white mana")
    void cannotActivateWithoutMana() {
        addWallReady();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Activation only lets the activated wall attack")
    void abilityOnlyAffectsActivatedWall() {
        addCreatureReady(player1, new MirrorWall());
        addCreatureReady(player1, new MirrorWall());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Cannot activate the ability with only nonwhite mana")
    void cannotActivateWithOnlyNonWhiteMana() {
        addCreatureReady(player1, new MirrorWall());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Attack permission begins only when the ability resolves")
    void permissionRequiresResolution() {
        Permanent wall = addWallReady();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(als.canAttack(gd, wall, player1.getId())).isFalse();

        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        assertThat(wall.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("A tapped wall can activate but cannot attack until untapped")
    void tappedWallCanActivateButCannotAttack() {
        Permanent wall = addWallReady();
        wall.setTapped(true);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wall.isTapped()).isTrue();
        assertThat(als.canAttack(gd, wall, player1.getId())).isFalse();

        wall.setTapped(false);
        declareAttackersAndPrepareBlockers(List.of(0));
        assertThat(wall.isAttacking()).isTrue();
    }
}
