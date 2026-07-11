package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UnstoppableAshTest extends BaseCardTest {

    @Test
    @DisplayName("When another creature you control becomes blocked, it gets +0/+5 until end of turn")
    void allyBecomesBlockedGetsBoost() {
        Permanent bears = addReadyBears(player1);
        bears.setAttacking(true);
        addReadyAsh(player1);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Unstoppable Ash");
        assertThat(trigger.getSourcePermanentId()).isEqualTo(bears.getId());
        assertThat(trigger.isNonTargeting()).isTrue();

        harness.passBothPriorities();

        assertThat(bears.getToughnessModifier()).isEqualTo(5);
        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Unstoppable Ash boosts itself when it becomes blocked")
    void selfBecomesBlockedGetsBoost() {
        Permanent ash = addReadyAsh(player1);
        ash.setAttacking(true);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(ash.getToughnessModifier()).isEqualTo(5);
        assertThat(ash.getEffectiveToughness()).isEqualTo(10);
    }

    @Test
    @DisplayName("No becomes-blocked trigger when the creature is unblocked")
    void unblockedCreatesNoTrigger() {
        Permanent bears = addReadyBears(player1);
        bears.setAttacking(true);
        addReadyAsh(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Champion ETB auto-sacrifices with no Treefolk or Warrior to exile")
    void championAutoSacrificesWithoutValidCreature() {
        castAsh();
        harness.passBothPriorities(); // resolve champion ETB -> auto-sacrifice

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Unstoppable Ash"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Unstoppable Ash"));
    }

    @Test
    @DisplayName("Champion exiles a Warrior and keeps Unstoppable Ash")
    void championExilesWarrior() {
        harness.addToBattlefield(player1, new ElvishWarrior());
        castAsh();
        harness.passBothPriorities(); // resolve champion ETB -> permanent choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        UUID warriorId = harness.getPermanentId(player1, "Elvish Warrior");
        harness.handlePermanentChosen(player1, warriorId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Unstoppable Ash"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Elvish Warrior"));
    }

    private void castAsh() {
        harness.setHand(player1, List.of(new UnstoppableAsh()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> ETB on stack
    }

    private Permanent addReadyAsh(Player player) {
        Permanent perm = new Permanent(new UnstoppableAsh());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyBears(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
