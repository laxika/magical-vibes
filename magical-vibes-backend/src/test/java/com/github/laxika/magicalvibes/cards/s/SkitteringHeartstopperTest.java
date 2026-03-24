package com.github.laxika.magicalvibes.cards.s;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkitteringHeartstopperTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Skittering Heartstopper has one activated ability granting deathtouch")
    void hasOneActivatedAbility() {
        SkitteringHeartstopper card = new SkitteringHeartstopper();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{B}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect deathtouch = (GrantKeywordEffect) card.getActivatedAbilities().get(0).getEffects().getFirst();
        assertThat(deathtouch.keywords()).containsExactly(Keyword.DEATHTOUCH);
        assertThat(deathtouch.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== Deathtouch ability =====

    @Test
    @DisplayName("Activating deathtouch ability puts it on the stack")
    void activatingDeathtouchPutsOnStack() {
        Permanent heartstopper = addHeartstopperReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Skittering Heartstopper");
        assertThat(entry.getTargetId()).isEqualTo(heartstopper.getId());
    }

    @Test
    @DisplayName("Resolving deathtouch ability grants deathtouch until end of turn")
    void resolvingDeathtouchAbilityGrantsDeathtouch() {
        Permanent heartstopper = addHeartstopperReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, heartstopper, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Deathtouch granted by ability resets at end of turn cleanup")
    void deathtouchResetsAtEndOfTurn() {
        Permanent heartstopper = addHeartstopperReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, heartstopper, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, heartstopper, Keyword.DEATHTOUCH)).isFalse();
    }

    // ===== Activation constraints =====

    @Test
    @DisplayName("Activating ability does NOT tap Skittering Heartstopper")
    void activatingAbilityDoesNotTap() {
        Permanent heartstopper = addHeartstopperReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(heartstopper.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addHeartstopperReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate ability when tapped")
    void canActivateWhenTapped() {
        Permanent heartstopper = addHeartstopperReady(player1);
        heartstopper.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Skittering Heartstopper");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if Skittering Heartstopper is removed before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addHeartstopperReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helper methods =====

    private Permanent addHeartstopperReady(Player player) {
        Permanent perm = new Permanent(new SkitteringHeartstopper());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
