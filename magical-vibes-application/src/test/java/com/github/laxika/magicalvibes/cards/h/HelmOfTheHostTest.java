package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HelmOfTheHostTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Helm of the Host has beginning of combat triggered effect")
    void hasBeginningOfCombatTriggeredEffect() {
        HelmOfTheHost card = new HelmOfTheHost();

        assertThat(card.getEffects(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED).getFirst())
                .isInstanceOf(CreateTokenCopyOfEquippedCreatureEffect.class);

        CreateTokenCopyOfEquippedCreatureEffect effect =
                (CreateTokenCopyOfEquippedCreatureEffect) card.getEffects(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED).getFirst();
        assertThat(effect.removeLegendary()).isTrue();
        assertThat(effect.grantHaste()).isTrue();
    }

    @Test
    @DisplayName("Helm of the Host has equip {5} ability")
    void hasEquipAbility() {
        HelmOfTheHost card = new HelmOfTheHost();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{5}");
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().getFirst().isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getTargetFilter())
                .isInstanceOf(ControlledPermanentPredicateTargetFilter.class);
        assertThat(card.getActivatedAbilities().getFirst().getTimingRestriction())
                .isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst())
                .isInstanceOf(EquipEffect.class);
    }

    // ===== Equip ability =====

    @Test
    @DisplayName("Resolving equip ability attaches Helm to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent helm = addHelmReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(helm.getAttachedTo()).isEqualTo(creature.getId());
    }

    // ===== Beginning of combat trigger =====

    @Test
    @DisplayName("At beginning of combat, creates a token copy of equipped creature")
    void createsTokenCopyAtBeginningOfCombat() {
        Permanent creature = addReadyCreature(player1);
        Permanent helm = addHelmReady(player1);
        helm.setAttachedTo(creature.getId());

        // Advance from precombat main to beginning of combat
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to BEGINNING_OF_COMBAT, triggers fire
        harness.passBothPriorities(); // resolve the triggered ability

        // Should have the original creature + a token copy
        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken())
                .count();
        assertThat(tokenCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Token copy has haste")
    void tokenCopyHasHaste() {
        Permanent creature = addReadyCreature(player1);
        Permanent helm = addHelmReady(player1);
        helm.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken())
                .findFirst().orElse(null);
        assertThat(token).isNotNull();
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
    }

    @Test
    @DisplayName("Token copy of legendary creature is not legendary")
    void tokenCopyNotLegendary() {
        Permanent legendary = addLegendaryCreature(player1);
        Permanent helm = addHelmReady(player1);
        helm.setAttachedTo(legendary.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hallar, the Firefletcher") && p.getCard().isToken())
                .findFirst().orElse(null);
        assertThat(token).isNotNull();
        assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
    }

    @Test
    @DisplayName("Token copy of non-legendary creature preserves supertypes")
    void tokenCopyPreservesNonLegendarySupertypes() {
        Permanent creature = addReadyCreature(player1);
        Permanent helm = addHelmReady(player1);
        helm.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken())
                .findFirst().orElse(null);
        assertThat(token).isNotNull();
        // Grizzly Bears has no supertypes, so the token shouldn't either
        assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
    }

    @Test
    @DisplayName("Token copy has same power and toughness as equipped creature")
    void tokenCopySamePowerToughness() {
        Permanent creature = addReadyCreature(player1);
        Permanent helm = addHelmReady(player1);
        helm.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken())
                .findFirst().orElse(null);
        assertThat(token).isNotNull();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("No token created when Helm is not attached to any creature")
    void noTokenWhenNotAttached() {
        addReadyCreature(player1);
        addHelmReady(player1);
        // Helm is NOT attached

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .count();
        assertThat(tokenCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Trigger only fires on controller's turn")
    void triggerOnlyOnControllersTurn() {
        Permanent creature = addReadyCreature(player1);
        Permanent helm = addHelmReady(player1);
        helm.setAttachedTo(creature.getId());

        // Opponent's turn — trigger should not fire
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .count();
        assertThat(tokenCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Equipped creature leaving before resolution means no token")
    void equippedCreatureLeavesBeforeResolution() {
        Permanent creature = addReadyCreature(player1);
        Permanent helm = addHelmReady(player1);
        helm.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to BEGINNING_OF_COMBAT, trigger on stack

        // Remove the equipped creature before resolution
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        helm.setAttachedTo(null);

        harness.passBothPriorities(); // resolve

        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .count();
        assertThat(tokenCount).isEqualTo(0);
    }

    // ===== Helpers =====

    private Permanent addHelmReady(Player player) {
        Permanent perm = new Permanent(new HelmOfTheHost());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addLegendaryCreature(Player player) {
        Permanent perm = new Permanent(new HallarTheFirefletcher());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
