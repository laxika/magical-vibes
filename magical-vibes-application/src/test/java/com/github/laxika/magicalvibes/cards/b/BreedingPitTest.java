package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BreedingPit.class)
class BreedingPitTest extends BaseCardTest {

    // ===== Upkeep sacrifice-unless-pay =====

    @Test
    @DisplayName("Declining to pay {B}{B} sacrifices Breeding Pit")
    void decliningPaymentSacrificesEnchantment() {
        harness.addToBattlefield(player1, new BreedingPit());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Breeding Pit");
        harness.assertInGraveyard(player1, "Breeding Pit");
    }

    @Test
    @DisplayName("Paying {B}{B} keeps Breeding Pit on the battlefield")
    void payingKeepsEnchantment() {
        harness.addToBattlefield(player1, new BreedingPit());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Breeding Pit");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Accepting without {B}{B} still sacrifices Breeding Pit")
    void acceptingPaymentWithoutEnoughManaSacrificesEnchantment() {
        harness.addToBattlefield(player1, new BreedingPit());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Breeding Pit");
        harness.assertInGraveyard(player1, "Breeding Pit");
    }

    // ===== End step token creation =====

    @Test
    @DisplayName("A 0/1 black Thrull token is created at the controller's end step")
    void endStepCreatesThrullToken() {
        harness.addToBattlefield(player1, new BreedingPit());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player1, TurnStep.END_STEP); // advance to end step -> trigger queued
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        harness.passBothPriorities(); // resolve trigger -> token created

        var thrulls = findPermanents(player1, "Thrull");
        assertThat(thrulls).hasSize(1);
        assertThat(thrulls).allSatisfy(t -> {
            assertThat(t.getCard().getPower()).isEqualTo(0);
            assertThat(t.getCard().getToughness()).isEqualTo(1);
            assertThat(t.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(t.getCard().getSubtypes()).containsExactly(CardSubtype.THRULL);
            assertThat(t.getCard().isToken()).isTrue();
        });
    }

    @Test
    @DisplayName("No Thrull token is created during the opponent's end step")
    void doesNotTriggerDuringOpponentEndStep() {
        harness.addToBattlefield(player1, new BreedingPit());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        harness.assertNotOnBattlefield(player1, "Thrull");
    }
}
