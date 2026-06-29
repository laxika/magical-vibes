package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SerumRakerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Serum Raker has ON_DEATH effect that makes each player discard a card")
    void hasDeathTriggerEffect() {
        SerumRaker card = new SerumRaker();

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEATH).getFirst())
                .isInstanceOf(EachPlayerDiscardsEffect.class);
        EachPlayerDiscardsEffect effect = (EachPlayerDiscardsEffect) card.getEffects(EffectSlot.ON_DEATH).getFirst();
        assertThat(effect.amount()).isEqualTo(1);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Serum Raker puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new SerumRaker()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Serum Raker"));
    }

    // ===== Death trigger =====

    @Test
    @DisplayName("When Serum Raker dies in combat, death trigger goes on the stack")
    void deathTriggerGoesOnStack() {
        harness.addToBattlefield(player1, new SerumRaker());

        setupCombatWhereSerumRakerDies();
        harness.passBothPriorities(); // Combat damage — Serum Raker dies

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Serum Raker"));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Serum Raker");
    }

    @Test
    @DisplayName("Resolving death trigger prompts active player to discard first (APNAP)")
    void deathTriggerPromptsActivePlayerFirst() {
        harness.addToBattlefield(player1, new SerumRaker());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        setupCombatWhereSerumRakerDies();
        harness.passBothPriorities(); // Combat damage — Serum Raker dies
        harness.passBothPriorities(); // Resolve death trigger

        // Active player (player1) should be prompted to discard first
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.cardChoice().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.revealedHandChoice().discardRemainingCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("After active player discards, non-active player is prompted")
    void afterActivePlayerDiscardsNonActivePlayerPrompted() {
        harness.addToBattlefield(player1, new SerumRaker());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        setupCombatWhereSerumRakerDies();
        harness.passBothPriorities(); // Combat damage — Serum Raker dies
        harness.passBothPriorities(); // Resolve death trigger

        // Active player discards
        harness.handleCardChosen(player1, 0);

        // Non-active player should now be prompted
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.cardChoice().playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.revealedHandChoice().discardRemainingCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Both players discard a card when death trigger fully resolves")
    void bothPlayersDiscardOnDeath() {
        harness.addToBattlefield(player1, new SerumRaker());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        setupCombatWhereSerumRakerDies();
        harness.passBothPriorities(); // Combat damage — Serum Raker dies
        harness.passBothPriorities(); // Resolve death trigger

        // Both players discard
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Death trigger skips player with empty hand and prompts the other")
    void skipsPlayerWithEmptyHand() {
        harness.addToBattlefield(player1, new SerumRaker());
        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        setupCombatWhereSerumRakerDies();
        harness.passBothPriorities(); // Combat damage — Serum Raker dies
        harness.passBothPriorities(); // Resolve death trigger

        // Active player (player1) has no cards — should skip to player2
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.cardChoice().playerId()).isEqualTo(player2.getId());
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no cards to discard"));
    }

    @Test
    @DisplayName("Death trigger does nothing when both players have empty hands")
    void doesNothingWhenBothHandsEmpty() {
        harness.addToBattlefield(player1, new SerumRaker());
        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, new ArrayList<>());

        setupCombatWhereSerumRakerDies();
        harness.passBothPriorities(); // Combat damage — Serum Raker dies
        harness.passBothPriorities(); // Resolve death trigger

        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    // ===== Helpers =====

    private void setupCombatWhereSerumRakerDies() {
        Permanent serumRakerPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Serum Raker"))
                .findFirst().orElseThrow();
        serumRakerPerm.setSummoningSick(false);
        serumRakerPerm.setAttacking(true);

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(5);
        bigBear.setToughness(5);
        Permanent blockerPerm = new Permanent(bigBear);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
