package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BreedingPitTest extends BaseCardTest {

    // ===== Upkeep sacrifice-unless-pay =====

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP, trigger fires
    }

    @Test
    @DisplayName("Declining to pay {B}{B} sacrifices Breeding Pit")
    void decliningPaymentSacrificesEnchantment() {
        harness.addToBattlefield(player1, new BreedingPit());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Breeding Pit"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Breeding Pit"));
    }

    @Test
    @DisplayName("Paying {B}{B} keeps Breeding Pit on the battlefield")
    void payingKeepsEnchantment() {
        harness.addToBattlefield(player1, new BreedingPit());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Breeding Pit"));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    // ===== End step token creation =====

    @Test
    @DisplayName("A 0/1 black Thrull token is created at the controller's end step")
    void endStepCreatesThrullToken() {
        harness.addToBattlefield(player1, new BreedingPit());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities(); // advance to end step -> trigger queued
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        harness.passBothPriorities(); // resolve trigger -> token created

        var thrulls = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Thrull"))
                .toList();
        assertThat(thrulls).hasSize(1);
        assertThat(thrulls).allSatisfy(t -> {
            assertThat(t.getCard().getPower()).isEqualTo(0);
            assertThat(t.getCard().getToughness()).isEqualTo(1);
            assertThat(t.getCard().isToken()).isTrue();
        });
    }

    @Test
    @DisplayName("No Thrull token is created during the opponent's end step")
    void doesNotTriggerDuringOpponentEndStep() {
        harness.addToBattlefield(player1, new BreedingPit());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Pass through player2's end step; no controller end-step trigger fires for player1,
        // so priority passing runs on until player1's next upkeep trigger stops it.
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Thrull"));
    }
}
