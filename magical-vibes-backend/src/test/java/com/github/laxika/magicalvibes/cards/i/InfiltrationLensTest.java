package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InfiltrationLensTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Infiltration Lens has ON_BECOMES_BLOCKED MayEffect with DrawCardEffect(2)")
    void hasOnBecomesBlockedEffect() {
        InfiltrationLens card = new InfiltrationLens();

        assertThat(card.getEffects(EffectSlot.ON_BECOMES_BLOCKED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_BECOMES_BLOCKED).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_BECOMES_BLOCKED).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(DrawCardEffect.class);
        assertThat(((DrawCardEffect) mayEffect.wrapped()).amount()).isEqualTo(2);
        assertThat(mayEffect.triggersPerBlocker()).isTrue();
    }

    @Test
    @DisplayName("Infiltration Lens has equip {1} ability")
    void hasEquipAbility() {
        InfiltrationLens card = new InfiltrationLens();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{1}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getTargetFilter())
                .isInstanceOf(ControlledPermanentPredicateTargetFilter.class);
        assertThat(card.getActivatedAbilities().get(0).getTimingRestriction())
                .isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(EquipEffect.class);
    }

    // ===== Equip ability =====

    @Test
    @DisplayName("Resolving equip ability attaches Infiltration Lens to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent lens = addLens(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(lens.getAttachedTo()).isEqualTo(creature.getId());
    }

    // ===== Becomes-blocked trigger =====

    @Test
    @DisplayName("When equipped creature becomes blocked by one creature, one trigger is created")
    void singleBlockerCreatesOneTrigger() {
        Permanent creature = addReadyCreature(player1);
        Permanent lens = addLens(player1);
        lens.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        addReadyCreature(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        long lensTriggers = gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Infiltration Lens"))
                .count();
        assertThat(lensTriggers).isEqualTo(1);
    }

    @Test
    @DisplayName("When equipped creature becomes blocked by two creatures, two triggers are created")
    void multipleBlockersCreateMultipleTriggers() {
        Permanent creature = addReadyCreature(player1);
        Permanent lens = addLens(player1);
        lens.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        addReadyCreature(player2);
        addReadyCreature(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        long lensTriggers = gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Infiltration Lens"))
                .count();
        assertThat(lensTriggers).isEqualTo(2);
    }

    @Test
    @DisplayName("Accepting the may ability draws two cards")
    void acceptingMayAbilityDrawsTwoCards() {
        Permanent creature = addReadyCreature(player1);
        Permanent lens = addLens(player1);
        lens.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        addReadyCreature(player2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Resolve the MayEffect trigger
        harness.passBothPriorities();
        // Accept the "you may draw two cards" prompt (puts DrawCardEffect on stack)
        harness.handleMayAbilityChosen(player1, true);
        // Resolve the DrawCardEffect triggered ability
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Declining the may ability does not draw cards")
    void decliningMayAbilityDoesNotDraw() {
        Permanent creature = addReadyCreature(player1);
        Permanent lens = addLens(player1);
        lens.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        addReadyCreature(player2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Resolve the MayEffect trigger
        harness.passBothPriorities();
        // Decline the "you may draw two cards" prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("No trigger when unequipped creature is blocked")
    void noTriggerWhenNotEquipped() {
        Permanent creature = addReadyCreature(player1);
        addLens(player1); // Lens on battlefield but not attached
        creature.setAttacking(true);

        addReadyCreature(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        long lensTriggers = gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Infiltration Lens"))
                .count();
        assertThat(lensTriggers).isZero();
    }

    @Test
    @DisplayName("Two blockers: accepting both may abilities draws four cards total")
    void twoBlockersAcceptBothDrawsFourCards() {
        Permanent creature = addReadyCreature(player1);
        Permanent lens = addLens(player1);
        lens.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        addReadyCreature(player2);
        addReadyCreature(player2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        // Resolve first MayEffect trigger
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        // Resolve the accepted DrawCardEffect
        harness.passBothPriorities();
        // Resolve second MayEffect trigger
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        // Resolve the accepted DrawCardEffect
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 4);
    }

    // ===== Trigger metadata =====

    @Test
    @DisplayName("Trigger is a triggered ability with correct source")
    void triggerHasCorrectMetadata() {
        Permanent creature = addReadyCreature(player1);
        Permanent lens = addLens(player1);
        lens.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        addReadyCreature(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Infiltration Lens")
                        && se.getSourcePermanentId().equals(lens.getId())
                        && se.getControllerId().equals(player1.getId()));
    }

    // ===== Helpers =====

    private Permanent addLens(Player player) {
        Permanent perm = new Permanent(new InfiltrationLens());
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

    private void setupDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);
    }
}
