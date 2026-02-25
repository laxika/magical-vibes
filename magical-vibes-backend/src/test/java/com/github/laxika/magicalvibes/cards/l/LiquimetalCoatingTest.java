package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AddCardTypeToTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LiquimetalCoatingTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has correct activated ability structure")
    void hasCorrectAbility() {
        LiquimetalCoating card = new LiquimetalCoating();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(AddCardTypeToTargetPermanentEffect.class);
        AddCardTypeToTargetPermanentEffect effect = (AddCardTypeToTargetPermanentEffect) ability.getEffects().getFirst();
        assertThat(effect.cardType()).isEqualTo(CardType.ARTIFACT);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new LiquimetalCoating()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Liquimetal Coating");
    }

    @Test
    @DisplayName("Resolving puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new LiquimetalCoating()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Liquimetal Coating"));
    }

    // ===== Activated ability: targeting a creature =====

    @Test
    @DisplayName("Activating ability targets a creature and puts ability on the stack")
    void activatingTargetingCreaturePutsOnStack() {
        Permanent coating = addReadyCoating(player1);
        Permanent target = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetPermanentId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolving ability makes target creature an artifact")
    void resolvingMakesCreatureAnArtifact() {
        addReadyCoating(player1);
        Permanent target = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getGrantedCardTypes()).contains(CardType.ARTIFACT);
        assertThat(gqs.isArtifact(target)).isTrue();
    }

    @Test
    @DisplayName("Target creature retains its creature type")
    void targetRetainsCreatureType() {
        addReadyCoating(player1);
        Permanent target = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.isArtifact(target)).isTrue();
    }

    // ===== Activated ability: targeting a land =====

    @Test
    @DisplayName("Can target a land and make it an artifact")
    void canTargetLand() {
        addReadyCoating(player1);
        Permanent targetLand = addReadyLand(player2);

        harness.activateAbility(player1, 0, null, targetLand.getId());
        harness.passBothPriorities();

        assertThat(targetLand.getGrantedCardTypes()).contains(CardType.ARTIFACT);
        assertThat(gqs.isArtifact(targetLand)).isTrue();
    }

    // ===== Activated ability: targeting an enchantment =====

    @Test
    @DisplayName("Can target an enchantment and make it an artifact")
    void canTargetEnchantment() {
        addReadyCoating(player1);
        Permanent enchantment = addReadyEnchantment(player2);

        harness.activateAbility(player1, 0, null, enchantment.getId());
        harness.passBothPriorities();

        assertThat(enchantment.getGrantedCardTypes()).contains(CardType.ARTIFACT);
        assertThat(gqs.isArtifact(enchantment)).isTrue();
    }

    // ===== Activated ability: targeting an artifact =====

    @Test
    @DisplayName("Can target an already-artifact permanent (no-op but legal)")
    void canTargetAlreadyArtifact() {
        addReadyCoating(player1);
        Permanent artifact = addReadyArtifact(player2);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(artifact)).isTrue();
    }

    // ===== End of turn cleanup =====

    @Test
    @DisplayName("Artifact type wears off at end of turn")
    void artifactTypeWearsOffAtEndOfTurn() {
        addReadyCoating(player1);
        Permanent target = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(target)).isTrue();

        // Advance through END_STEP to trigger CLEANUP which resets end-of-turn modifiers
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getGrantedCardTypes()).isEmpty();
        assertThat(gqs.isArtifact(target)).isFalse();
    }

    // ===== Taps when activated =====

    @Test
    @DisplayName("Activating taps Liquimetal Coating")
    void activatingTapsCoating() {
        Permanent coating = addReadyCoating(player1);
        Permanent target = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(coating.isTapped()).isTrue();
    }

    // ===== Cannot activate when tapped =====

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent coating = addReadyCoating(player1);
        coating.tap();
        Permanent target = addReadyCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyCoating(player1);
        Permanent target = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Game log =====

    @Test
    @DisplayName("Resolving ability logs the type change")
    void resolvingLogsTypeChange() {
        addReadyCoating(player1);
        Permanent target = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Grizzly Bears") && log.contains("becomes an") && log.contains("Artifact"));
    }

    // ===== Helpers =====

    private Permanent addReadyCoating(Player player) {
        LiquimetalCoating card = new LiquimetalCoating();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyLand(Player player) {
        Forest card = new Forest();
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyArtifact(Player player) {
        AngelsFeather card = new AngelsFeather();
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyEnchantment(Player player) {
        Pacifism card = new Pacifism();
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
