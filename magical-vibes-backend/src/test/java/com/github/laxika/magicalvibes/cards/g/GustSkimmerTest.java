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
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GustSkimmerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Gust-Skimmer has one activated ability granting flying")
    void hasFlyingActivatedAbility() {
        GustSkimmer card = new GustSkimmer();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{U}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect flying = (GrantKeywordEffect) card.getActivatedAbilities().get(0).getEffects().getFirst();
        assertThat(flying.keyword()).isEqualTo(Keyword.FLYING);
        assertThat(flying.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Gust-Skimmer puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new GustSkimmer()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Gust-Skimmer");
    }

    @Test
    @DisplayName("Resolving puts Gust-Skimmer onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new GustSkimmer()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Gust-Skimmer"));
    }

    // ===== Flying ability =====

    @Test
    @DisplayName("Activating flying ability puts it on the stack")
    void activatingFlyingPutsOnStack() {
        Permanent skimmer = addSkimmerReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Gust-Skimmer");
        assertThat(entry.getTargetPermanentId()).isEqualTo(skimmer.getId());
    }

    @Test
    @DisplayName("Resolving flying ability grants flying until end of turn")
    void resolvingFlyingAbilityGrantsFlying() {
        Permanent skimmer = addSkimmerReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, skimmer, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying granted by ability resets at end of turn cleanup")
    void flyingResetsAtEndOfTurn() {
        Permanent skimmer = addSkimmerReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, skimmer, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, skimmer, Keyword.FLYING)).isFalse();
    }

    // ===== Activation constraints =====

    @Test
    @DisplayName("Activating ability does NOT tap Gust-Skimmer")
    void activatingAbilityDoesNotTap() {
        Permanent skimmer = addSkimmerReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(skimmer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without blue mana")
    void cannotActivateWithoutBlueMana() {
        addSkimmerReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate ability when tapped")
    void canActivateWhenTapped() {
        Permanent skimmer = addSkimmerReady(player1);
        skimmer.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Gust-Skimmer");
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness")
    void canActivateWithSummoningSickness() {
        Permanent skimmer = new Permanent(new GustSkimmer());
        gd.playerBattlefields.get(player1.getId()).add(skimmer);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Gust-Skimmer");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if Gust-Skimmer is removed before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addSkimmerReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helper methods =====

    private Permanent addSkimmerReady(Player player) {
        Permanent perm = new Permanent(new GustSkimmer());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
