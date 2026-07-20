package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SanctumGuardianTest extends BaseCardTest {

    // ===== Activation / source choice =====

    @Test
    @DisplayName("Activating the ability sacrifices the Guardian and puts the ability on the stack")
    void activatingSacrificesAndPutsOnStack() {
        Permanent guardian = addReady(player1, new SanctumGuardian());

        harness.activateAbility(player1, indexOf(player1, guardian), null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Sanctum Guardian"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sanctum Guardian"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving the ability prompts for a source choice")
    void resolvingPromptsForSourceChoice() {
        Permanent guardian = addReady(player1, new SanctumGuardian());
        addReadyStats(player2, 2, 2);

        harness.activateAbility(player1, indexOf(player1, guardian), null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Choosing a source records a one-shot any-target prevention shield")
    void choosingSourceRecordsShield() {
        Permanent guardian = addReady(player1, new SanctumGuardian());
        Permanent source = addReadyStats(player2, 2, 2);

        harness.activateAbility(player1, indexOf(player1, guardian), null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.sourceNextDamageToAnyTargetShields).containsExactly(source.getId());
    }

    // ===== Noncombat damage =====

    @Test
    @DisplayName("Prevents the next noncombat damage from the chosen source to a creature and is consumed")
    void preventsNoncombatDamageToCreature() {
        Permanent guardian = addReady(player1, new SanctumGuardian());
        Permanent pyromancer = addReady(player1, new ProdigalPyromancer());
        Permanent creature = addReadyStats(player2, 3, 3);

        harness.activateAbility(player1, indexOf(player1, guardian), null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, pyromancer.getId());

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents the next noncombat damage from the chosen source to a player")
    void preventsNoncombatDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent guardian = addReady(player1, new SanctumGuardian());
        Permanent pyromancer = addReady(player1, new ProdigalPyromancer());

        harness.activateAbility(player1, indexOf(player1, guardian), null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, pyromancer.getId());

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Damage from a source other than the chosen one is not prevented")
    void doesNotAffectNonChosenSource() {
        Permanent guardian = addReady(player1, new SanctumGuardian());
        Permanent pyromancer = addReady(player1, new ProdigalPyromancer());
        Permanent decoy = addReadyStats(player1, 2, 2);
        Permanent creature = addReadyStats(player2, 3, 3);

        harness.activateAbility(player1, indexOf(player1, guardian), null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, decoy.getId());

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.sourceNextDamageToAnyTargetShields).containsExactly(decoy.getId());
    }

    // ===== Combat damage =====

    @Test
    @DisplayName("Prevents combat damage from the chosen attacker to the defending player")
    void preventsCombatDamageToPlayer() {
        harness.setLife(player1, 20);
        Permanent guardian = addReady(player1, new SanctumGuardian());
        Permanent attacker = addReadyStats(player2, 2, 2);

        harness.activateAbility(player1, indexOf(player1, guardian), null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());

        harness.forceActivePlayer(player2);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents combat damage from the chosen attacker to a blocking creature")
    void preventsCombatDamageToCreature() {
        Permanent guardian = addReady(player1, new SanctumGuardian());
        Permanent blocker = addReadyStats(player1, 3, 3);
        Permanent attacker = addReadyStats(player2, 2, 2);

        harness.activateAbility(player1, indexOf(player1, guardian), null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, attacker.getId());

        harness.forceActivePlayer(player2);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(indexOf(player1, blocker), 0)));
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    // ===== Cleanup / no valid sources =====

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        Permanent guardian = addReady(player1, new SanctumGuardian());
        Permanent source = addReadyStats(player2, 2, 2);

        harness.activateAbility(player1, indexOf(player1, guardian), null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.sourceNextDamageToAnyTargetShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("No permanents on the battlefield leaves no prompt and no shield")
    void noPermanentsNoShield() {
        Permanent guardian = addReady(player1, new SanctumGuardian());

        harness.activateAbility(player1, indexOf(player1, guardian), null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("No permanents on the battlefield"));
    }

    @Test
    @DisplayName("Answering the source choice resumes the parked resolution entry")
    void answeringSourceChoiceClearsParkedResolution() {
        Permanent guardian = addReady(player1, new SanctumGuardian());
        Permanent source = addReadyStats(player2, 2, 2);

        harness.activateAbility(player1, indexOf(player1, guardian), null, null);
        harness.passBothPriorities();
        assertThat(gd.pendingEffectResolutionEntry).isNotNull();

        harness.handlePermanentChosen(player1, source.getId());

        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.deferPlayerLossCheck).isFalse();
    }

    // ===== Helpers =====

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyStats(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
