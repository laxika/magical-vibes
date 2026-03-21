package com.github.laxika.magicalvibes.cards.d;

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

class DeathHoodCobraTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Death-Hood Cobra has two activated abilities granting reach and deathtouch")
    void hasTwoActivatedAbilities() {
        DeathHoodCobra card = new DeathHoodCobra();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        // First ability: reach
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{1}{G}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect reach = (GrantKeywordEffect) card.getActivatedAbilities().get(0).getEffects().getFirst();
        assertThat(reach.keywords()).containsExactly(Keyword.REACH);
        assertThat(reach.scope()).isEqualTo(GrantScope.SELF);

        // Second ability: deathtouch
        assertThat(card.getActivatedAbilities().get(1).getManaCost()).isEqualTo("{1}{G}");
        assertThat(card.getActivatedAbilities().get(1).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(1).isNeedsTarget()).isFalse();
        assertThat(card.getActivatedAbilities().get(1).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(1).getEffects().getFirst())
                .isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect deathtouch = (GrantKeywordEffect) card.getActivatedAbilities().get(1).getEffects().getFirst();
        assertThat(deathtouch.keywords()).containsExactly(Keyword.DEATHTOUCH);
        assertThat(deathtouch.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== Reach ability =====

    @Test
    @DisplayName("Activating reach ability puts it on the stack")
    void activatingReachPutsOnStack() {
        Permanent cobra = addCobraReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Death-Hood Cobra");
        assertThat(entry.getTargetId()).isEqualTo(cobra.getId());
    }

    @Test
    @DisplayName("Resolving reach ability grants reach until end of turn")
    void resolvingReachAbilityGrantsReach() {
        Permanent cobra = addCobraReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, cobra, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Reach granted by ability resets at end of turn cleanup")
    void reachResetsAtEndOfTurn() {
        Permanent cobra = addCobraReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cobra, Keyword.REACH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cobra, Keyword.REACH)).isFalse();
    }

    // ===== Deathtouch ability =====

    @Test
    @DisplayName("Activating deathtouch ability puts it on the stack")
    void activatingDeathtouchPutsOnStack() {
        Permanent cobra = addCobraReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Death-Hood Cobra");
        assertThat(entry.getTargetId()).isEqualTo(cobra.getId());
    }

    @Test
    @DisplayName("Resolving deathtouch ability grants deathtouch until end of turn")
    void resolvingDeathtouchAbilityGrantsDeathtouch() {
        Permanent cobra = addCobraReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, cobra, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Deathtouch granted by ability resets at end of turn cleanup")
    void deathtouchResetsAtEndOfTurn() {
        Permanent cobra = addCobraReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cobra, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cobra, Keyword.DEATHTOUCH)).isFalse();
    }

    // ===== Both abilities =====

    @Test
    @DisplayName("Can activate both abilities to gain both reach and deathtouch")
    void canActivateBothAbilities() {
        Permanent cobra = addCobraReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cobra, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, cobra, Keyword.DEATHTOUCH)).isTrue();
    }

    // ===== Activation constraints =====

    @Test
    @DisplayName("Activating ability does NOT tap Death-Hood Cobra")
    void activatingAbilityDoesNotTap() {
        Permanent cobra = addCobraReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(cobra.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCobraReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate ability when tapped")
    void canActivateWhenTapped() {
        Permanent cobra = addCobraReady(player1);
        cobra.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Death-Hood Cobra");
    }

    // ===== Deathtouch combat interaction =====

    @Test
    @DisplayName("Death-Hood Cobra with deathtouch kills blocker regardless of toughness")
    void deathtouchKillsBlocker() {
        Permanent cobra = addCobraReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Activate deathtouch
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cobra, Keyword.DEATHTOUCH)).isTrue();

        // Set up combat with a large blocker
        harness.addToBattlefield(player2, new DeathHoodCobra());
        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getLast();
        blocker.setSummoningSick(false);

        cobra.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Blocker should be dead from deathtouch
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(blocker.getId()));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if Death-Hood Cobra is removed before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addCobraReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helper methods =====

    private Permanent addCobraReady(Player player) {
        Permanent perm = new Permanent(new DeathHoodCobra());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
