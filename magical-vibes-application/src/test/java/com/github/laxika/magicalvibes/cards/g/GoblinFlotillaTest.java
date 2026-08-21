package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RiverMerfolk;
import com.github.laxika.magicalvibes.model.Keyword;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinFlotilla.class, RiverMerfolk.class})
class GoblinFlotillaTest extends BaseCardTest {

    @Test
    @DisplayName("Declining the beginning-of-combat payment gives the blocked attacker first strike")
    void decliningPaymentGrantsFirstStrikeToAttacker() {
        Permanent flotilla = addCreatureReady(player2, new GoblinFlotilla());
        Permanent attacker = addCreatureReady(player1, new RiverMerfolk());
        attacker.setAttacking(true);

        declineBeginningOfCombatPayment(player1, player2);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, flotilla, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Declining the beginning-of-combat payment gives the blocked creature first strike")
    void decliningPaymentGrantsFirstStrikeToBlocker() {
        Permanent flotilla = addCreatureReady(player1, new GoblinFlotilla());
        flotilla.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RiverMerfolk());

        declineBeginningOfCombatPayment(player1, player1);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, blocker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, flotilla, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Paying the beginning-of-combat cost prevents the first-strike trigger")
    void payingPreventsFirstStrikeTrigger() {
        Permanent flotilla = addCreatureReady(player1, new GoblinFlotilla());
        flotilla.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RiverMerfolk());

        beginCombatAndPay(player1);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, blocker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Declining the beginning-of-combat payment grants first strike to every blocker")
    void decliningPaymentGrantsFirstStrikeToEveryBlocker() {
        Permanent flotilla = addCreatureReady(player1, new GoblinFlotilla());
        flotilla.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new RiverMerfolk());
        Permanent secondBlocker = addCreatureReady(player2, new RiverMerfolk());

        declineBeginningOfCombatPayment(player1, player1);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, firstBlocker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondBlocker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, flotilla, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike granted by the Flotilla wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        Permanent flotilla = addCreatureReady(player1, new GoblinFlotilla());
        flotilla.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RiverMerfolk());

        declineBeginningOfCombatPayment(player1, player1);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, blocker, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, blocker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Accepting the beginning-of-combat payment without mana still uses the fallback")
    void acceptingPaymentWithoutManaUsesFallback() {
        Permanent flotilla = addCreatureReady(player2, new GoblinFlotilla());
        Permanent attacker = addCreatureReady(player1, new RiverMerfolk());
        attacker.setAttacking(true);

        beginCombatAndChoosePayment(player1, player2, false);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, flotilla, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @CardUsed({Island.class})
    @DisplayName("Islandwalk prevents blocking while the defending player controls an Island")
    void islandwalkPreventsBlockingWithIsland() {
        harness.addToBattlefield(player2, new Island());
        Permanent blocker = addCreatureReady(player2, new RiverMerfolk());
        Permanent flotilla = addCreatureReady(player1, new GoblinFlotilla());
        flotilla.setAttacking(true);

        prepareDeclareBlockers(player1);

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(flotilla);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    private void declineBeginningOfCombatPayment(Player activePlayer, Player payer) {
        beginCombat(activePlayer);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(payer, false);
    }

    private void beginCombatAndChoosePayment(Player activePlayer, Player payer, boolean addMana) {
        beginCombat(activePlayer);
        if (addMana) {
            harness.addMana(payer, ManaColor.RED, 1);
        }
        harness.handleMayAbilityChosen(payer, true);
    }

    private void beginCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
    }

    private void beginCombatAndPay(Player activePlayer) {
        beginCombatAndChoosePayment(activePlayer, activePlayer, true);
    }
}
