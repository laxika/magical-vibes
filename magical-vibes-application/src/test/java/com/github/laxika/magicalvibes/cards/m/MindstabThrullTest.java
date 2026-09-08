package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindstabThrull.class})
class MindstabThrullTest extends BaseCardTest {

    private Permanent addAttacker() {
        return addCreatureReady(player1, new MindstabThrull());
    }

    private int attackerIndex(Permanent attacker) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
    }

    private void declareUnblockedAttack(Permanent attacker) {
        declareAttackers(List.of(attackerIndex(attacker)));
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Accepting the may sacrifices the Thrull and the defending player discards three cards")
    void unblockedAcceptSacrificeAndDiscardThree() {
        harness.setHand(player2, List.of(new MindstabThrull(), new MindstabThrull(), new MindstabThrull()));
        declareUnblockedAttack(addAttacker());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        // The Thrull is sacrificed as part of accepting.
        harness.assertNotOnBattlefield(player1, "Mindstab Thrull");
        harness.assertInGraveyard(player1, "Mindstab Thrull");

        // Defending player discards three cards.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(3);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Declining the may keeps the Thrull and forces no discard")
    void unblockedDeclineKeepsThrull() {
        harness.setHand(player2, List.of(new MindstabThrull(), new MindstabThrull(), new MindstabThrull()));
        declareUnblockedAttack(addAttacker());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Mindstab Thrull");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("\"If you do\" gate — Thrull removed in response means no sacrifice and no discard")
    void unblockedNoDiscardWhenThrullLeavesBeforeResolution() {
        harness.setHand(player2, List.of(new MindstabThrull(), new MindstabThrull(), new MindstabThrull()));
        Permanent thrull = addAttacker();
        declareUnblockedAttack(thrull);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        // The Thrull is removed while the trigger waits on the may choice — with no sacrifice the
        // contingent discard must not happen.
        gd.playerBattlefields.get(player1.getId()).remove(thrull);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Blocked attacker does not trigger the ability")
    void blockedNoTrigger() {
        harness.setHand(player2, List.of(new MindstabThrull(), new MindstabThrull(), new MindstabThrull()));

        Permanent blocker = addCreatureReady(player2, new MindstabThrull());

        Permanent attacker = addAttacker();
        declareAttackers(List.of(attackerIndex(attacker)));
        resolveAllTriggers();

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker), attackerIndex(attacker))));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Mindstab Thrull");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("An attacker triggers when the defender could block but chooses not to")
    void unblockedWhenDefenderDeclinesToBlock() {
        harness.setHand(player2, List.of(new MindstabThrull(), new MindstabThrull(), new MindstabThrull()));
        addCreatureReady(player2, new MindstabThrull());
        Permanent attacker = addAttacker();

        declareAttackers(List.of(attackerIndex(attacker)));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        harness.assertOnBattlefield(player1, "Mindstab Thrull");
    }

    @Test
    @DisplayName("Accepting the may discards only the cards available in a smaller hand")
    void unblockedAcceptSacrificeAndDiscardAvailableCards() {
        harness.setHand(player2, List.of(new MindstabThrull(), new MindstabThrull()));
        declareUnblockedAttack(addAttacker());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(3);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Accepting the may with an empty hand sacrifices the Thrull without prompting for discards")
    void unblockedAcceptSacrificeWithEmptyHand() {
        harness.setHand(player2, List.of());
        declareUnblockedAttack(addAttacker());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Mindstab Thrull");
        harness.assertInGraveyard(player1, "Mindstab Thrull");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }
}
