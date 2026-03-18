package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SelfHasKeywordConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManorGargoyleTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Manor Gargoyle has correct static effect and activated ability")
    void hasCorrectEffectsAndAbilities() {
        ManorGargoyle card = new ManorGargoyle();

        // Static effect: indestructible as long as it has defender
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        var conditional = (SelfHasKeywordConditionalEffect) card.getEffects(EffectSlot.STATIC).get(0);
        assertThat(conditional.keyword()).isEqualTo(Keyword.DEFENDER);
        assertThat(conditional.wrapped()).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect indestructible = (GrantKeywordEffect) conditional.wrapped();
        assertThat(indestructible.keyword()).isEqualTo(Keyword.INDESTRUCTIBLE);
        assertThat(indestructible.scope()).isEqualTo(GrantScope.SELF);

        // Activated ability: {1} loses defender and gains flying
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{1}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(2);

        assertThat(card.getActivatedAbilities().get(0).getEffects().get(0))
                .isInstanceOf(RemoveKeywordEffect.class);
        RemoveKeywordEffect removeDefender = (RemoveKeywordEffect) card.getActivatedAbilities().get(0).getEffects().get(0);
        assertThat(removeDefender.keyword()).isEqualTo(Keyword.DEFENDER);
        assertThat(removeDefender.scope()).isEqualTo(GrantScope.SELF);

        assertThat(card.getActivatedAbilities().get(0).getEffects().get(1))
                .isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect grantFlying = (GrantKeywordEffect) card.getActivatedAbilities().get(0).getEffects().get(1);
        assertThat(grantFlying.keyword()).isEqualTo(Keyword.FLYING);
        assertThat(grantFlying.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== Indestructible while having defender =====

    @Test
    @DisplayName("Manor Gargoyle has indestructible while it has defender")
    void hasIndestructibleWithDefender() {
        Permanent gargoyle = addGargoyleReady(player1);

        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Manor Gargoyle loses indestructible when it loses defender via activated ability")
    void losesIndestructibleWhenDefenderRemoved() {
        Permanent gargoyle = addGargoyleReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Manor Gargoyle regains indestructible at end of turn when defender returns")
    void regainsIndestructibleAtEndOfTurn() {
        Permanent gargoyle = addGargoyleReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    // ===== Indestructible survives destruction =====

    @Test
    @DisplayName("Indestructible Manor Gargoyle survives Wrath of God")
    void indestructibleSurvivesWrathOfGod() {
        addGargoyleReady(player1);
        harness.addToBattlefield(player2, new ManorGargoyle());

        // Cast Wrath of God
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        // Both gargoyles should survive (both have defender → indestructible)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Manor Gargoyle"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Manor Gargoyle"));
    }

    @Test
    @DisplayName("Manor Gargoyle without defender does not survive Wrath of God")
    void vulnerableWithoutDefender() {
        Permanent gargoyle = addGargoyleReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Activate ability to lose defender
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.INDESTRUCTIBLE)).isFalse();

        // Cast Wrath of God
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        // Gargoyle should be destroyed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Manor Gargoyle"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Manor Gargoyle"));
    }

    // ===== Activated ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingAbilityPutsOnStack() {
        addGargoyleReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Manor Gargoyle");
    }

    @Test
    @DisplayName("Resolving ability removes defender and grants flying until end of turn")
    void resolvingAbilityRemovesDefenderAndGrantsFlying() {
        Permanent gargoyle = addGargoyleReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Defender and flying reset at end of turn")
    void defenderAndFlyingResetAtEndOfTurn() {
        Permanent gargoyle = addGargoyleReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.FLYING)).isFalse();
    }

    // ===== Combat interaction =====

    @Test
    @DisplayName("Cannot attack without activating ability (has defender)")
    void cannotAttackWithDefender() {
        addGargoyleReady(player1);
        harness.addToBattlefield(player2, new ManorGargoyle());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginAttackerDeclaration(player1.getId());

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Can attack after activating ability (defender removed)")
    void canAttackAfterActivatingAbility() {
        Permanent gargoyle = addGargoyleReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addToBattlefield(player2, new ManorGargoyle());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginAttackerDeclaration(player1.getId());

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gargoyle.isAttacking()).isTrue();
    }

    // ===== Activation constraints =====

    @Test
    @DisplayName("Activating ability does NOT tap Manor Gargoyle")
    void activatingAbilityDoesNotTap() {
        Permanent gargoyle = addGargoyleReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gargoyle.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addGargoyleReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Static bonus survives end-of-turn reset =====

    @Test
    @DisplayName("Static indestructible survives end-of-turn modifier reset")
    void staticIndestructibleSurvivesEndOfTurnReset() {
        Permanent gargoyle = addGargoyleReady(player1);

        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.INDESTRUCTIBLE)).isTrue();

        // Simulate end-of-turn cleanup
        gargoyle.resetModifiers();

        // Static keyword should still be computed
        assertThat(gqs.hasKeyword(gd, gargoyle, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    // ===== Helper methods =====

    private Permanent addGargoyleReady(Player player) {
        Permanent perm = new Permanent(new ManorGargoyle());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
