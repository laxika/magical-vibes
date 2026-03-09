package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AnimateLandEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpawningPoolTest extends BaseCardTest {


    // ===== Card properties =====

    @Test
    @DisplayName("Spawning Pool has correct card properties")
    void hasCorrectProperties() {
        SpawningPool card = new SpawningPool();

        assertThat(card.isEntersTapped()).isTrue();
        assertThat(card.getActivatedAbilities()).hasSize(2);

        var animateAbility = card.getActivatedAbilities().get(0);
        assertThat(animateAbility.getManaCost()).isEqualTo("{1}{B}");
        assertThat(animateAbility.isRequiresTap()).isFalse();
        assertThat(animateAbility.getEffects()).hasSize(1);
        assertThat(animateAbility.getEffects().getFirst()).isInstanceOf(AnimateLandEffect.class);

        var regenAbility = card.getActivatedAbilities().get(1);
        assertThat(regenAbility.getManaCost()).isEqualTo("{B}");
        assertThat(regenAbility.isRequiresTap()).isFalse();
        assertThat(regenAbility.getEffects()).hasSize(1);
        assertThat(regenAbility.getEffects().getFirst()).isInstanceOf(RegenerateEffect.class);
    }

    // ===== Enters the battlefield tapped =====

    @Test
    @DisplayName("Spawning Pool enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new SpawningPool()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent pool = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Spawning Pool"))
                .findFirst().orElseThrow();
        assertThat(pool.isTapped()).isTrue();
    }

    // ===== Tap for mana =====

    @Test
    @DisplayName("Tapping Spawning Pool produces black mana")
    void tappingProducesBlackMana() {
        Permanent pool = addPoolReady(player1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(pool);

        gs.tapPermanent(gd, player1, index);

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
        assertThat(entry.getCard().getName()).isEqualTo("Spawning Pool");
        assertThat(entry.getTargetPermanentId()).isEqualTo(pool.getId());
    }

    @Test
    @DisplayName("Resolving animate ability makes it a 1/1 black Skeleton creature")
    void resolvingAbilityMakesItA1x1BlackSkeleton() {
        Permanent pool = addPoolReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(pool.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(pool.getAnimatedPower()).isEqualTo(1);
        assertThat(pool.getAnimatedToughness()).isEqualTo(1);
        assertThat(gqs.isCreature(gd, pool)).isTrue();
        assertThat(gqs.getEffectivePower(gd, pool)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, pool)).isEqualTo(1);
        assertThat(pool.getAnimatedColor()).isEqualTo(CardColor.BLACK);
        assertThat(pool.getTransientSubtypes()).containsExactly(CardSubtype.SKELETON);
    }

    @Test
    @DisplayName("Spawning Pool is still a land while animated")
    void stillALandWhileAnimated() {
        Permanent pool = addPoolReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(pool.getCard().getType()).isEqualTo(CardType.LAND);
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

        assertThat(pool.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, pool)).isFalse();
        assertThat(pool.getTransientSubtypes()).isEmpty();
        assertThat(pool.getAnimatedColor()).isNull();
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

    // ===== Not a creature before activation =====

    @Test
    @DisplayName("Spawning Pool is not a creature before activation")
    void notACreatureBeforeActivation() {
        Permanent pool = addPoolReady(player1);

        assertThat(gqs.isCreature(gd, pool)).isFalse();
        assertThat(pool.getCard().getType()).isEqualTo(CardType.LAND);
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
        SpawningPool card = new SpawningPool();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
