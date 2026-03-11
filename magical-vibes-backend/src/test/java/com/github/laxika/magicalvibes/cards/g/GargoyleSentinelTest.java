package com.github.laxika.magicalvibes.cards.g;

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
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GargoyleSentinelTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Gargoyle Sentinel has one activated ability with two effects")
    void hasActivatedAbility() {
        GargoyleSentinel card = new GargoyleSentinel();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{3}");
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

    // ===== Ability resolution =====

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent sentinel = addSentinelReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Gargoyle Sentinel");
    }

    @Test
    @DisplayName("Resolving ability removes defender and grants flying until end of turn")
    void resolvingAbilityRemovesDefenderAndGrantsFlying() {
        Permanent sentinel = addSentinelReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Defender and flying reset at end of turn")
    void defenderAndFlyingResetAtEndOfTurn() {
        Permanent sentinel = addSentinelReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.FLYING)).isFalse();
    }

    // ===== Combat interaction =====

    @Test
    @DisplayName("Cannot attack without activating ability (has defender)")
    void cannotAttackWithDefender() {
        addSentinelReady(player1);
        harness.addToBattlefield(player2, new GargoyleSentinel());

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
        Permanent sentinel = addSentinelReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addToBattlefield(player2, new GargoyleSentinel());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginAttackerDeclaration(player1.getId());

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(sentinel.isAttacking()).isTrue();
    }

    // ===== Activation constraints =====

    @Test
    @DisplayName("Activating ability does NOT tap Gargoyle Sentinel")
    void activatingAbilityDoesNotTap() {
        Permanent sentinel = addSentinelReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(sentinel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addSentinelReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if Gargoyle Sentinel is removed before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addSentinelReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helper methods =====

    private Permanent addSentinelReady(Player player) {
        Permanent perm = new Permanent(new GargoyleSentinel());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
