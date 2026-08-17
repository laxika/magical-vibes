package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinFlotillaTest extends BaseCardTest {

    @Test
    @DisplayName("Declining the beginning-of-combat payment gives the blocked attacker first strike")
    void decliningPaymentGrantsFirstStrikeToAttacker() {
        Permanent flotilla = addReadyFlotilla(player2);
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);

        declineBeginningOfCombatPayment(player1, player2);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(attacker.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
        assertThat(flotilla.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Declining the beginning-of-combat payment gives the blocked creature first strike")
    void decliningPaymentGrantsFirstStrikeToBlocker() {
        Permanent flotilla = addReadyFlotilla(player1);
        flotilla.setAttacking(true);
        Permanent blocker = addReadyCreature(player2);

        declineBeginningOfCombatPayment(player1, player1);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(blocker.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
        assertThat(flotilla.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Paying the beginning-of-combat cost prevents the first-strike trigger")
    void payingPreventsFirstStrikeTrigger() {
        Permanent flotilla = addReadyFlotilla(player1);
        flotilla.setAttacking(true);
        Permanent blocker = addReadyCreature(player2);

        beginCombatAndPay(player1);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(blocker.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    private void declineBeginningOfCombatPayment(Player activePlayer, Player payer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(payer, false);
    }

    private void beginCombatAndPay(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.addMana(activePlayer, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(activePlayer, true);
    }

    private Permanent addReadyFlotilla(Player player) {
        return putReadyCreature(player, new GoblinFlotilla());
    }

    private Permanent addReadyCreature(Player player) {
        return putReadyCreature(player, new GrizzlyBears());
    }

    private Permanent putReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
