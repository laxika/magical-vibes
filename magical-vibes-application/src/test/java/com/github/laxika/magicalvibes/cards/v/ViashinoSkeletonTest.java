package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ViashinoSkeletonTest extends BaseCardTest {

    // ===== Activate regeneration ability =====

    @Test
    @DisplayName("Activating the ability with mana and a card in hand starts the discard-cost choice")
    void activationStartsDiscardChoice() {
        addSkeletonReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing a card discards it and puts the regeneration ability on the stack")
    void choosingCardPaysCostAndStacksAbility() {
        Permanent skeleton = addSkeletonReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(skeleton.getId());
    }

    @Test
    @DisplayName("Resolving the ability grants a regeneration shield")
    void resolvingGrantsRegenerationShield() {
        Permanent skeleton = addSkeletonReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(skeleton.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate the ability without a card in hand to discard")
    void cannotActivateWithoutCardInHand() {
        addSkeletonReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addSkeletonReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Regeneration saves from combat =====

    @Test
    @DisplayName("Regeneration shield saves Viashino Skeleton from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent skeleton = addSkeletonReady(player1);
        skeleton.setRegenerationShield(1);
        skeleton.setBlocking(true);
        skeleton.addBlockingTarget(0);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Viashino Skeleton");
        assertThat(skeleton.isTapped()).isTrue();
        assertThat(skeleton.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Viashino Skeleton dies in lethal combat without a regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent skeleton = addSkeletonReady(player1);
        skeleton.setBlocking(true);
        skeleton.addBlockingTarget(0);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Viashino Skeleton");
        harness.assertInGraveyard(player1, "Viashino Skeleton");
    }

    // ===== Helper =====

    private Permanent addSkeletonReady(Player player) {
        Permanent perm = new Permanent(new ViashinoSkeleton());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
