package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindstabThrull.class, Unsummon.class})
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
    @DisplayName("Returning the Thrull to hand in response prevents the sacrifice and discard")
    void unblockedNoDiscardWhenThrullLeavesBeforeResolution() {
        harness.setHand(player1, List.of(new Unsummon()));
        harness.setHand(player2, List.of(new MindstabThrull(), new MindstabThrull(), new MindstabThrull()));
        Permanent thrull = addAttacker();
        declareAttackersAndPrepareBlockers(List.of(attackerIndex(thrull)));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, thrull.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Mindstab Thrull");
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInGraveyard(player1, "Mindstab Thrull");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Blocked attacker does not trigger the ability")
    void blockedNoTrigger() {
        harness.setHand(player2, List.of(new MindstabThrull(), new MindstabThrull(), new MindstabThrull()));

        Permanent blocker = addCreatureReady(player2, new MindstabThrull());

        Permanent attacker = addAttacker();
        declareAttackersAndPrepareBlockers(List.of(attackerIndex(attacker)));

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

        declareAttackersAndPrepareBlockers(List.of(attackerIndex(attacker)));
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

    @Test
    @DisplayName("The defending player chooses exactly three cards from a larger hand")
    void defendingPlayerChoosesThreeAndKeepsTheRemainingCard() {
        MindstabThrull kept = new MindstabThrull();
        MindstabThrull firstDiscard = new MindstabThrull();
        MindstabThrull secondDiscard = new MindstabThrull();
        MindstabThrull thirdDiscard = new MindstabThrull();
        harness.setHand(player1, List.of(new MindstabThrull()));
        harness.setHand(player2, List.of(kept, firstDiscard, secondDiscard, thirdDiscard));
        declareUnblockedAttack(addAttacker());

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, 1);
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(firstDiscard, secondDiscard, thirdDiscard);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Mindstab Thrull");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("When player two attacks, player two sacrifices and player one discards")
    void defendingPlayerIsCorrectWhenPlayerTwoAttacks() {
        harness.setHand(player1, List.of(new MindstabThrull(), new MindstabThrull(), new MindstabThrull()));
        harness.setHand(player2, List.of(new MindstabThrull()));
        addCreatureReady(player2, new MindstabThrull());
        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        resolveCombat(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Mindstab Thrull");
        harness.assertNotOnBattlefield(player2, "Mindstab Thrull");
        harness.assertLife(player1, 20);
    }
}
