package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.m.MyrServitor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EonHub.class, MyrServitor.class})
class EonHubTest extends BaseCardTest {

    @Test
    @DisplayName("Players skip their upkeep steps")
    void playersSkipTheirUpkeepSteps() {
        harness.addToBattlefield(player1, new EonHub());
        harness.addToBattlefield(player1, new MyrServitor());
        harness.addToBattlefield(player2, new MyrServitor());
        harness.setGraveyard(player1, List.of(new MyrServitor()));
        harness.setGraveyard(player2, List.of(new MyrServitor()));

        skipUpkeep(player1);
        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
        harness.assertInGraveyard(player1, "Myr Servitor");
        harness.assertInGraveyard(player2, "Myr Servitor");

        skipUpkeep(player2);
        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
        harness.assertInGraveyard(player1, "Myr Servitor");
        harness.assertInGraveyard(player2, "Myr Servitor");
    }

    private void skipUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(activePlayer, TurnStep.PRECOMBAT_MAIN);
    }
}
