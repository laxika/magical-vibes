package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TemporalExtortion.class)
class TemporalExtortionTest extends BaseCardTest {

    @Test
    @DisplayName("Takes an extra turn when no player pays half their life")
    void takesExtraTurnWhenNoPlayerPays() {
        castTemporalExtortion();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        harness.assertInGraveyard(player1, "Temporal Extortion");
    }

    @Test
    @DisplayName("A player may pay half their life rounded up to counter it")
    void paysHalfLifeRoundedUpToCounter() {
        harness.setLife(player1, 19);
        castTemporalExtortion();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gd.extraTurns).isEmpty();
        harness.assertLife(player1, 9);
        harness.assertInGraveyard(player1, "Temporal Extortion");
    }

    @Test
    @DisplayName("An opponent may pay their own half life rounded up to counter it")
    void opponentMayPayTheirOwnHalfLifeToCounter() {
        harness.setLife(player2, 19);
        castTemporalExtortion();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.extraTurns).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 9);
        harness.assertInGraveyard(player1, "Temporal Extortion");
    }

    @Test
    @DisplayName("Remaining players may still pay after the spell is countered")
    void bothPlayersMayPay() {
        harness.setLife(player1, 19);
        harness.setLife(player2, 14);
        castTemporalExtortion();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInGraveyard(player1, "Temporal Extortion");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player1, 9);
        harness.assertLife(player2, 7);
        assertThat(gd.extraTurns).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An even life total costs exactly half to counter the spell")
    void paysExactlyHalfOfEvenLifeTotal() {
        harness.setLife(player2, 20);
        castTemporalExtortion();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 10);
        harness.assertInGraveyard(player1, "Temporal Extortion");
        assertThat(gd.extraTurns).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void castTemporalExtortion() {
        harness.castFromHand(player1, new TemporalExtortion(), "{B}{B}{B}{B}");
    }
}
