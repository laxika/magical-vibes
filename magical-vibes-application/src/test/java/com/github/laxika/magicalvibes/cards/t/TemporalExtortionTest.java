package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    private void castTemporalExtortion() {
        harness.setHand(player1, List.of(new TemporalExtortion()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, 0);
    }
}
