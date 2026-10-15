package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GhostWarden;
import com.github.laxika.magicalvibes.cards.g.Gristleback;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PredatoryFocus.class, GhostWarden.class, Gristleback.class})
class PredatoryFocusTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting makes a blocked creature assign all combat damage to the player")
    void acceptingAssignsBlockedDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new Gristleback());
        Permanent blocker = addCreatureReady(player2, new Gristleback());

        castAndChoose(true);
        declareBlockedAttack(player1, attacker, blocker);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Declining leaves blocked combat damage assigned to the blocker")
    void decliningUsesNormalCombatDamageAssignment() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new Gristleback());
        Permanent blocker = addCreatureReady(player2, new Gristleback());

        castAndChoose(false);
        declareBlockedAttack(player1, attacker, blocker);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Assigning damage as though unblocked does not prevent the blocker dealing damage")
    void blockerStillDealsCombatDamageToAttacker() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new GhostWarden());
        Permanent blocker = addCreatureReady(player2, new Gristleback());

        castAndChoose(true);
        declareBlockedAttack(player1, attacker, blocker);
        resolveCombat();

        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Accepting also affects creatures entering later that turn")
    void affectsCreaturesEnteringLaterThatTurn() {
        harness.setLife(player2, 20);
        castAndChoose(true);

        Permanent attacker = addCreatureReady(player1, new GhostWarden());
        Permanent blocker = addCreatureReady(player2, new Gristleback());
        declareBlockedAttack(player1, attacker, blocker);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("The choice is made while Predatory Focus resolves")
    void asksForChoiceOnResolution() {
        harness.setHand(player1, List.of(new PredatoryFocus()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting affects only creatures controlled by Predatory Focus's controller")
    void affectsOnlyTheControllersCreatures() {
        harness.setLife(player1, 20);
        Permanent attacker = addCreatureReady(player2, new Gristleback());
        Permanent blocker = addCreatureReady(player1, new Gristleback());

        castAndChoose(true);
        declareBlockedAttack(player2, attacker, blocker);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("The accepted effect expires at the end of the turn")
    void expiresAtEndOfTurn() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new Gristleback());
        Permanent blocker = addCreatureReady(player2, new Gristleback());

        castAndChoose(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UNTAP);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        declareBlockedAttack(player1, attacker, blocker);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    private void castAndChoose(boolean accept) {
        harness.setHand(player1, List.of(new PredatoryFocus()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
    }

    private void declareBlockedAttack(Player attackerController, Permanent attacker, Permanent blocker) {
        declareAttackersAndPrepareBlockers(attackerController,
                List.of(gd.playerBattlefields.get(attackerController.getId()).indexOf(attacker)));
        Player defender = attackerController == player1 ? player2 : player1;
        gs.declareBlockers(gd, defender, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(defender.getId()).indexOf(blocker),
                gd.playerBattlefields.get(attackerController.getId()).indexOf(attacker))));
    }
}
