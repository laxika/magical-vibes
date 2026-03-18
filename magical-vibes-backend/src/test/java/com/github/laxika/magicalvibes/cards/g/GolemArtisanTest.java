package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenKeywordToTargetEffect;
import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GolemArtisanTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Golem Artisan has two activated abilities")
    void hasTwoActivatedAbilities() {
        GolemArtisan card = new GolemArtisan();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        // First ability: {2} +1/+1
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{2}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().get(0))
                .isInstanceOf(BoostTargetCreatureEffect.class);

        // Second ability: {2} choose flying/trample/haste
        assertThat(card.getActivatedAbilities().get(1).getManaCost()).isEqualTo("{2}");
        assertThat(card.getActivatedAbilities().get(1).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(1).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(1).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(1).getEffects().get(0))
                .isInstanceOf(GrantChosenKeywordToTargetEffect.class);
        GrantChosenKeywordToTargetEffect keywordEffect =
                (GrantChosenKeywordToTargetEffect) card.getActivatedAbilities().get(1).getEffects().get(0);
        assertThat(keywordEffect.options()).containsExactly(Keyword.FLYING, Keyword.TRAMPLE, Keyword.HASTE);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Golem Artisan puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new GolemArtisan()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Golem Artisan");
    }

    @Test
    @DisplayName("Resolving puts Golem Artisan onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new GolemArtisan()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Golem Artisan"));
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new GolemArtisan()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== +1/+1 ability =====

    @Test
    @DisplayName("Activating +1/+1 ability puts BoostTargetCreature on the stack")
    void activatingPutsPlusOneOnStack() {
        addGolemReady(player1);
        Permanent target = addArtifactCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Golem Artisan");
        assertThat(entry.getTargetPermanentId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolving +1/+1 ability grants +1/+1 until end of turn")
    void resolvingPlusOneAbilityGrantsBoost() {
        addGolemReady(player1);
        Permanent target = addArtifactCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        int originalPower = gqs.getEffectivePower(gd, target);
        int originalToughness = gqs.getEffectiveToughness(gd, target);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(originalToughness + 1);
    }

    @Test
    @DisplayName("+1/+1 boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addGolemReady(player1);
        Permanent target = addArtifactCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        // Gold Myr is 1/1, +1/+1 = 2/2
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
    }

    // ===== Keyword choice ability =====

    @Test
    @DisplayName("Activating keyword ability puts GrantChosenKeyword on the stack")
    void activatingKeywordAbilityPutsOnStack() {
        addGolemReady(player1);
        Permanent target = addArtifactCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Golem Artisan");
        assertThat(entry.getTargetPermanentId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolving keyword ability prompts for keyword choice then grants flying")
    void resolvingKeywordAbilityGrantsFlying() {
        addGolemReady(player1);
        Permanent target = addArtifactCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities(); // resolve effect -> prompts keyword choice

        // Player chooses flying
        harness.handleListChoice(player1, "FLYING");

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Resolving keyword ability can grant trample")
    void resolvingKeywordAbilityGrantsTrample() {
        addGolemReady(player1);
        Permanent target = addArtifactCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "TRAMPLE");

        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Resolving keyword ability can grant haste")
    void resolvingKeywordAbilityGrantsHaste() {
        addGolemReady(player1);
        Permanent target = addArtifactCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "HASTE");

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Keyword granted by ability resets at end of turn cleanup")
    void keywordResetsAtEndOfTurn() {
        addGolemReady(player1);
        Permanent target = addArtifactCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FLYING");

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Choosing invalid keyword is rejected")
    void choosingInvalidKeywordIsRejected() {
        addGolemReady(player1);
        Permanent target = addArtifactCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "FIRST_STRIKE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not among valid options");
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Can target own artifact creatures")
    void canTargetOwnArtifactCreature() {
        addGolemReady(player1);
        Permanent ownArtifactCreature = addArtifactCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, ownArtifactCreature.getId());
        harness.passBothPriorities();

        // Gold Myr is 1/1, +1/+1 = 2/2
        assertThat(gqs.getEffectivePower(gd, ownArtifactCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target non-artifact creature")
    void cannotTargetNonArtifactCreature() {
        addGolemReady(player1);
        GoblinPiker nonArtifactCreature = new GoblinPiker();
        Permanent perm = new Permanent(nonArtifactCreature);
        gd.playerBattlefields.get(player2.getId()).add(perm);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, perm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact creature");
    }

    @Test
    @DisplayName("Cannot target artifact that is not a creature")
    void cannotTargetNonCreatureArtifact() {
        addGolemReady(player1);
        AngelsFeather artifact = new AngelsFeather();
        Permanent perm = new Permanent(artifact);
        gd.playerBattlefields.get(player2.getId()).add(perm);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, perm.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Activation constraints =====

    @Test
    @DisplayName("Activating ability does NOT tap Golem Artisan")
    void activatingAbilityDoesNotTap() {
        Permanent golem = addGolemReady(player1);
        Permanent target = addArtifactCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());

        assertThat(golem.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability when tapped")
    void canActivateWhenTapped() {
        Permanent golem = addGolemReady(player1);
        golem.tap();
        Permanent target = addArtifactCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness")
    void canActivateWithSummoningSickness() {
        GolemArtisan card = new GolemArtisan();
        Permanent golem = new Permanent(card);
        gd.playerBattlefields.get(player1.getId()).add(golem);
        Permanent target = addArtifactCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addGolemReady(player1);
        addArtifactCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 0, null, gd.playerBattlefields.get(player2.getId()).getFirst().getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addGolemReady(player1);
        addArtifactCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, gd.playerBattlefields.get(player2.getId()).getFirst().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate same ability multiple times")
    void canActivateSameAbilityMultipleTimes() {
        addGolemReady(player1);
        Permanent target = addArtifactCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        // Gold Myr 1/1 + 1/+1 = 2
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        // 2 + 1 = 3
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addGolemReady(player1);
        Permanent target = addArtifactCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helper methods =====

    private Permanent addGolemReady(Player player) {
        GolemArtisan card = new GolemArtisan();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addArtifactCreature(Player player) {
        GoldMyr card = new GoldMyr();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
