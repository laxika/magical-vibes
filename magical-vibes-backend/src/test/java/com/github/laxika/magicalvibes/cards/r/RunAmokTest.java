package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RunAmokTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Run Amok has correct card properties")
    void hasCorrectProperties() {
        RunAmok card = new RunAmok();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);

        BoostTargetCreatureEffect boost = (BoostTargetCreatureEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(boost.powerBoost()).isEqualTo(3);
        assertThat(boost.toughnessBoost()).isEqualTo(3);

        GrantKeywordEffect trample = (GrantKeywordEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(trample.keywords()).containsExactly(Keyword.TRAMPLE);
        assertThat(trample.scope()).isEqualTo(GrantScope.TARGET);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Run Amok targeting an attacking creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new RunAmok()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, attacker.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Run Amok");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Resolving Run Amok gives +3/+3 and trample to target attacking creature")
    void resolvingBoostsAndGrantsTrample() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new RunAmok()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(3);
        assertThat(attacker.getToughnessModifier()).isEqualTo(3);
        assertThat(attacker.getEffectivePower()).isEqualTo(5);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(5);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        // Add an attacking creature so the spell is playable
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        // Add a non-attacking creature directly to get its ID
        Permanent nonAttacker = new Permanent(new GrizzlyBears());
        nonAttacker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(nonAttacker);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new RunAmok()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID nonAttackerId = nonAttacker.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, nonAttackerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    // ===== End of turn cleanup =====

    @Test
    @DisplayName("Boost and trample wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new RunAmok()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(0);
        assertThat(attacker.getToughnessModifier()).isEqualTo(0);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Run Amok fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new RunAmok()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, attacker.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Run Amok"));
    }
}
