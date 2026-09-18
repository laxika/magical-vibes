package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
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

@CardUsed({MirrorWall.class, SuntailHawk.class})
class MirrorWallTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack without activating the ability")
    void cannotAttackWithDefender() {
        addCreatureReady(player1, new MirrorWall());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Ability lets the wall attack this turn")
    void abilityAllowsAttack() {
        Permanent wall = addCreatureReady(player1, new MirrorWall());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        assertThat(wall.isAttacking()).isTrue();
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
    @DisplayName("Attack permission wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        addCreatureReady(player1, new MirrorWall());
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
        addCreatureReady(player1, new MirrorWall());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
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
}
