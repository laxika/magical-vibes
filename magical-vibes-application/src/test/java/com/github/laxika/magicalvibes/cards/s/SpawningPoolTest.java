package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SpawningPool.class)
class SpawningPoolTest extends BaseCardTest {

    // ===== Enters the battlefield tapped =====

    @Test
    @DisplayName("Spawning Pool enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new SpawningPool()));
        harness.playLand(player1, 0);

        Permanent pool = findPermanent(player1, "Spawning Pool");
        assertThat(pool.isTapped()).isTrue();
    }

    // ===== Tap for mana =====

    @Test
    @DisplayName("Tapping Spawning Pool produces black mana")
    void tappingProducesBlackMana() {
        Permanent pool = addPoolReady(player1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(pool);

        harness.tapPermanent(player1, index);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    // ===== Animate ability =====

    @Test
    @DisplayName("Activating animate ability puts it on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent pool = addPoolReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard()).isSameAs(pool.getCard());
        assertThat(entry.getTargetId()).isEqualTo(pool.getId());
    }

    @Test
    @DisplayName("Resolving animate ability makes it a 1/1 black Skeleton creature")
    void resolvingAbilityMakesItA1x1BlackSkeleton() {
        Permanent pool = addPoolReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, pool)).isTrue();
        assertThat(gqs.getEffectivePower(gd, pool)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, pool)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, pool)).containsExactly(CardColor.BLACK);
        assertThat(gqs.effectiveCreatureSubtypes(gd, pool)).containsExactly(CardSubtype.SKELETON);
    }

    @Test
    @DisplayName("Spawning Pool is still a land while animated")
    void stillALandWhileAnimated() {
        Permanent pool = addPoolReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, pool)).isTrue();
        assertThat(gqs.isCreature(gd, pool)).isTrue();
    }

    @Test
    @DisplayName("Activating animate ability does NOT tap the permanent")
    void activatingAbilityDoesNotTap() {
        Permanent pool = addPoolReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(pool.isTapped()).isFalse();
    }

    // ===== Animation resets at end of turn =====

    @Test
    @DisplayName("Animation resets at end of turn")
    void animationResetsAtEndOfTurn() {
        Permanent pool = addPoolReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, pool)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, pool)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, pool)).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, pool)).isEmpty();
    }

    // ===== Regeneration =====

    @Test
    @DisplayName("Regeneration ability puts shield on the stack")
    void regenerationAbilityPutsOnStack() {
        Permanent pool = addPoolReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        // Animate first
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        // Activate regeneration
        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Regeneration shield is set after resolving")
    void regenerationShieldSetAfterResolving() {
        Permanent pool = addPoolReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        // Animate first
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        // Activate and resolve regeneration
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(pool.getRegenerationShield()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Regeneration shield saves Spawning Pool from destruction")
    void regenerationShieldPreventsDestruction() {
        Permanent pool = addPoolReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        pool.setMarkedDamage(1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, pool));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pool);
        assertThat(pool.getRegenerationShield()).isZero();
        assertThat(pool.getMarkedDamage()).isZero();
        assertThat(pool.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Animating Spawning Pool consumes one generic and one black mana")
    void animationConsumesGenericAndBlackMana() {
        addPoolReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    // ===== Not a creature before activation =====

    @Test
    @DisplayName("Spawning Pool is not a creature before activation")
    void notACreatureBeforeActivation() {
        Permanent pool = addPoolReady(player1);

        assertThat(gqs.isCreature(gd, pool)).isFalse();
        assertThat(gqs.isLand(gd, pool)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate regeneration while not animated as a creature")
    void cannotRegenerateWhileNotCreature() {
        addPoolReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    // ===== Helper methods =====

    private Permanent addPoolReady(Player player) {
        return addCreatureReady(player, new SpawningPool());
    }
}
