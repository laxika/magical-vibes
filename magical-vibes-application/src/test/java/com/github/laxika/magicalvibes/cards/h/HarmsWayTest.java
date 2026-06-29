package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect;
import com.github.laxika.magicalvibes.cards.l.LeylineOfPunishment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HarmsWayTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Harm's Way has correct spell effect")
    void hasCorrectEffect() {
        HarmsWay card = new HarmsWay();

        assertThat(card.getEffects(EffectSlot.SPELL))
                .anyMatch(e -> e instanceof PreventDamageFromChosenSourceAndRedirectToAnyTargetEffect effect
                        && effect.amount() == 2);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Harm's Way targeting a player puts it on the stack")
    void castTargetingPlayerPutsOnStack() {
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Harm's Way");
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Casting Harm's Way targeting a creature puts it on the stack")
    void castTargetingCreaturePutsOnStack() {
        Permanent bear = addReadyCreature(player2);
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, bear.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bear.getId());
    }

    // ===== Resolution — source choice =====

    @Test
    @DisplayName("Resolving Harm's Way prompts for source choice")
    void resolvingPromptsForSourceChoice() {
        Permanent opponentCreature = addReadyCreature(player2);
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.PERMANENT_CHOICE)).isTrue();
    }

    @Test
    @DisplayName("Choosing a source creates a source damage redirect shield")
    void choosingSourceCreatesShield() {
        Permanent opponentCreature = addReadyCreature(player2);
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        assertThat(gd.sourceDamageRedirectShields).hasSize(1);
        assertThat(gd.sourceDamageRedirectShields.getFirst().protectedPlayerId()).isEqualTo(player1.getId());
        assertThat(gd.sourceDamageRedirectShields.getFirst().damageSourceId()).isEqualTo(opponentCreature.getId());
        assertThat(gd.sourceDamageRedirectShields.getFirst().remainingAmount()).isEqualTo(2);
        assertThat(gd.sourceDamageRedirectShields.getFirst().redirectTargetId()).isEqualTo(player2.getId());
    }

    // ===== Combat damage redirect to player =====

    @Test
    @DisplayName("Redirect shield prevents 2 combat damage and redirects to target player")
    void redirectsCombatDamageToPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent opponentCreature = addReadyCreature(player2);

        // Cast Harm's Way targeting player2, choose opponent's creature as source
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        // Set up combat: opponent's Grizzly Bears (2/2) attacks player1
        harness.forceActivePlayer(player2);
        opponentCreature.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Player1 takes 0 damage (2 prevented by redirect shield)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        // Player2 takes 2 damage (redirected)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Redirect shield partially consumed when source deals more than 2 damage")
    void partialRedirectWhenSourceDealsMoreThan2() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Use a creature with higher power — add a 4/4
        Permanent bigCreature = addReadyCreatureWithStats(player2, 4, 4);

        // Cast Harm's Way targeting player2, choose big creature as source
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bigCreature.getId());

        // Combat: big creature attacks player1
        harness.forceActivePlayer(player2);
        bigCreature.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Player1 takes 2 damage (4 - 2 prevented = 2)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        // Player2 takes 2 damage (redirected)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Combat damage redirect to creature =====

    @Test
    @DisplayName("Redirect shield can redirect damage to a creature target")
    void redirectsCombatDamageToCreature() {
        harness.setLife(player1, 20);
        Permanent opponentCreature = addReadyCreatureWithStats(player2, 2, 2);
        Permanent targetCreature = addReadyCreatureWithStats(player2, 3, 3);

        // Cast Harm's Way targeting opponent's 3/3 creature, choose the 2/2 as source
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, targetCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        // Combat: 2/2 creature attacks player1
        harness.forceActivePlayer(player2);
        opponentCreature.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Player1 takes 0 damage (2/2 creature, all 2 prevented)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        // The 3/3 creature should have received 2 damage (marked but not lethal)
        assertThat(targetCreature.getMarkedDamage()).isEqualTo(2);
    }

    // ===== Non-matching source =====

    @Test
    @DisplayName("Redirect shield does not affect damage from non-matching source")
    void doesNotAffectNonMatchingSource() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent creature1 = addReadyCreature(player2);
        Permanent creature2 = addReadyCreatureWithName(player2, "Other Creature");

        // Cast Harm's Way targeting player2, choose creature1 as source
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature1.getId());

        // Combat: creature2 (not the chosen source) attacks player1
        harness.forceActivePlayer(player2);
        creature2.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Player1 takes full damage from creature2 (not the chosen source)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        // Player2 takes no redirected damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Shield should still be active (not consumed)
        assertThat(gd.sourceDamageRedirectShields).hasSize(1);
    }

    // ===== Interaction with "damage can't be prevented" =====

    @Test
    @DisplayName("Redirect shield still works when damage can't be prevented (Leyline of Punishment)")
    void redirectWorksWhenDamageCantBePrevented() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent opponentCreature = addReadyCreature(player2);

        // Leyline of Punishment on battlefield — damage can't be prevented
        harness.addToBattlefield(player2, new LeylineOfPunishment());

        // Cast Harm's Way targeting player2, choose opponent's creature as source
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        // Combat: opponent's Grizzly Bears (2/2) attacks player1
        harness.forceActivePlayer(player2);
        opponentCreature.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Harm's Way is redirection (replacement), not prevention — it still works
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Shield cleanup =====

    @Test
    @DisplayName("Source redirect shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        Permanent opponentCreature = addReadyCreature(player2);
        harness.setHand(player1, List.of(new HarmsWay()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        assertThat(gd.sourceDamageRedirectShields).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.sourceDamageRedirectShields).isEmpty();
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreatureWithStats(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreatureWithName(Player player, String name) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
