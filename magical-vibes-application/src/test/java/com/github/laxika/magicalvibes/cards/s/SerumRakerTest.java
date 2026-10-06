package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.c.ConsecratedSphinx;
import com.github.laxika.magicalvibes.cards.t.TreasureMage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerumRaker.class, TreasureMage.class, ConsecratedSphinx.class})
class SerumRakerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Serum Raker puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new SerumRaker()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Serum Raker");
    }

    @Test
    @DisplayName("When Serum Raker dies in combat, death trigger goes on the stack")
    void deathTriggerGoesOnStack() {
        harness.addToBattlefield(player1, new SerumRaker());

        setupCombatWhereSerumRakerDies();
        resolveCombat(); // Combat damage - Serum Raker dies

        harness.assertInGraveyard(player1, "Serum Raker");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Serum Raker");
    }

    @Test
    @DisplayName("Resolving death trigger prompts active player to discard first (APNAP)")
    void deathTriggerPromptsActivePlayerFirst() {
        harness.addToBattlefield(player1, new SerumRaker());
        harness.setHand(player1, List.of(new TreasureMage()));
        harness.setHand(player2, List.of(new TreasureMage()));

        setupCombatWhereSerumRakerDies();
        resolveCombat(); // Combat damage - Serum Raker dies
        harness.passBothPriorities(); // Resolve death trigger

        // Active player (player1) should be prompted to discard first
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("After active player chooses, non-active player is prompted")
    void afterActivePlayerChoosesNonActivePlayerPrompted() {
        harness.addToBattlefield(player1, new SerumRaker());
        harness.setHand(player1, List.of(new TreasureMage()));
        harness.setHand(player2, List.of(new TreasureMage()));

        setupCombatWhereSerumRakerDies();
        resolveCombat(); // Combat damage - Serum Raker dies
        harness.passBothPriorities(); // Resolve death trigger

        // Active player chooses
        harness.handleCardChosen(player1, 0);

        // Non-active player should now be prompted
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Both players discard a card when death trigger fully resolves")
    void bothPlayersDiscardOnDeath() {
        harness.addToBattlefield(player1, new SerumRaker());
        harness.setHand(player1, List.of(new TreasureMage()));
        harness.setHand(player2, List.of(new TreasureMage()));

        setupCombatWhereSerumRakerDies();
        resolveCombat(); // Combat damage - Serum Raker dies
        harness.passBothPriorities(); // Resolve death trigger

        // Both players discard
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Treasure Mage");
        harness.assertInGraveyard(player2, "Treasure Mage");
    }

    @Test
    @DisplayName("Death trigger skips player with empty hand and prompts the other")
    void skipsPlayerWithEmptyHand() {
        harness.addToBattlefield(player1, new SerumRaker());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new TreasureMage()));

        setupCombatWhereSerumRakerDies();
        resolveCombat(); // Combat damage - Serum Raker dies
        harness.passBothPriorities(); // Resolve death trigger

        // Active player (player1) has no cards - should skip to player2
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no cards to discard"));
    }

    @Test
    @DisplayName("Death trigger does nothing when both players have empty hands")
    void doesNothingWhenBothHandsEmpty() {
        harness.addToBattlefield(player1, new SerumRaker());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        setupCombatWhereSerumRakerDies();
        resolveCombat(); // Combat damage - Serum Raker dies
        harness.passBothPriorities(); // Resolve death trigger

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Each player chooses before either chosen card is discarded")
    void discardIsDeferredUntilBothPlayersChoose() {
        harness.addToBattlefield(player1, new SerumRaker());
        harness.setHand(player1, List.of(new TreasureMage(), new SerumRaker()));
        harness.setHand(player2, List.of(new TreasureMage(), new SerumRaker()));

        setupCombatWhereSerumRakerDies();
        resolveCombat();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.assertInHand(player1, "Treasure Mage");
        harness.assertNotInGraveyard(player1, "Treasure Mage");

        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player1, "Treasure Mage");
        harness.assertInGraveyard(player2, "Treasure Mage");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupCombatWhereSerumRakerDies() {
        Permanent serumRakerPerm = findPermanent(player1, "Serum Raker");
        serumRakerPerm.setSummoningSick(false);
        serumRakerPerm.setAttacking(true);

        Permanent blockerPerm = addCreatureReady(player2, new ConsecratedSphinx());
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
    }
}
