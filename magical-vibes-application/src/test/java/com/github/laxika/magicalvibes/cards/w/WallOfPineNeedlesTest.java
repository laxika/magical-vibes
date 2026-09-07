package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.h.HyalopterousLemure;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfPineNeedles.class, HyalopterousLemure.class})
class WallOfPineNeedlesTest extends BaseCardTest {

    @Test
    @DisplayName("Activating regeneration ability puts it on the stack targeting the wall")
    void activatingAbilityPutsOnStack() {
        Permanent wall = addCreatureReady(player1, new WallOfPineNeedles());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(wall.getId());
    }

    @Test
    @DisplayName("Resolving regeneration ability grants a regeneration shield")
    void resolvingAbilityGrantsRegenerationShield() {
        addCreatureReady(player1, new WallOfPineNeedles());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent wall = findPermanent(player1, "Wall of Pine Needles");
        assertThat(wall.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Wall of Pine Needles from lethal combat damage")
    void regenerationShieldSavesFromLethalCombatDamage() {
        Permanent wall = addCreatureReady(player1, new WallOfPineNeedles());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        wall.setBlocking(true);
        wall.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, new HyalopterousLemure());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wall of Pine Needles");
        Permanent survivingWall = findPermanent(player1, "Wall of Pine Needles");
        assertThat(survivingWall.isTapped()).isTrue();
        assertThat(survivingWall.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Cannot activate regeneration ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new WallOfPineNeedles());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

}
