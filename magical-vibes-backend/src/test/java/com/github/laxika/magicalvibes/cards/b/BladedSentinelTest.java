package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BladedSentinelTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Bladed Sentinel has one activated ability granting vigilance")
    void hasVigilanceActivatedAbility() {
        BladedSentinel card = new BladedSentinel();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{W}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect vigilance = (GrantKeywordEffect) card.getActivatedAbilities().get(0).getEffects().getFirst();
        assertThat(vigilance.keyword()).isEqualTo(Keyword.VIGILANCE);
        assertThat(vigilance.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Bladed Sentinel puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new BladedSentinel()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Bladed Sentinel");
    }

    @Test
    @DisplayName("Resolving puts Bladed Sentinel onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new BladedSentinel()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Bladed Sentinel"));
    }

    // ===== Vigilance ability =====

    @Test
    @DisplayName("Activating vigilance ability puts it on the stack")
    void activatingVigilancePutsOnStack() {
        Permanent sentinel = addSentinelReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Bladed Sentinel");
        assertThat(entry.getTargetPermanentId()).isEqualTo(sentinel.getId());
    }

    @Test
    @DisplayName("Resolving vigilance ability grants vigilance until end of turn")
    void resolvingVigilanceAbilityGrantsVigilance() {
        Permanent sentinel = addSentinelReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Vigilance granted by ability resets at end of turn cleanup")
    void vigilanceResetsAtEndOfTurn() {
        Permanent sentinel = addSentinelReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.VIGILANCE)).isFalse();
    }

    // ===== Activation constraints =====

    @Test
    @DisplayName("Activating ability does NOT tap Bladed Sentinel")
    void activatingAbilityDoesNotTap() {
        Permanent sentinel = addSentinelReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(sentinel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without white mana")
    void cannotActivateWithoutWhiteMana() {
        addSentinelReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate ability when tapped")
    void canActivateWhenTapped() {
        Permanent sentinel = addSentinelReady(player1);
        sentinel.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Bladed Sentinel");
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness")
    void canActivateWithSummoningSickness() {
        Permanent sentinel = new Permanent(new BladedSentinel());
        gd.playerBattlefields.get(player1.getId()).add(sentinel);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Bladed Sentinel");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if Bladed Sentinel is removed before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addSentinelReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helper methods =====

    private Permanent addSentinelReady(Player player) {
        Permanent perm = new Permanent(new BladedSentinel());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
