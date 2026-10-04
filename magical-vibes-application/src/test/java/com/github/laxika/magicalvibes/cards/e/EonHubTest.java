package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.m.MyrServitor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

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

    @Test
    @DisplayName("Skipping upkeep still allows both players to draw for the turn")
    void skippingUpkeepDoesNotSkipDraw() {
        harness.addToBattlefield(player1, new EonHub());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new MyrServitor(), new MyrServitor()));
        harness.setLibrary(player2, List.of(new MyrServitor(), new MyrServitor()));
        gd.turnNumber = 3;

        skipUpkeep(player1);
        harness.assertInHand(player1, "Myr Servitor");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        skipUpkeep(player2);
        harness.assertInHand(player2, "Myr Servitor");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Upkeep triggers resume after the last Eon Hub leaves the battlefield")
    void upkeepResumesWithoutEonHub() {
        var hub = harness.addToBattlefieldAndReturn(player1, new EonHub());
        harness.addToBattlefield(player1, new MyrServitor());
        harness.setGraveyard(player1, List.of(new MyrServitor()));

        skipUpkeep(player1);
        harness.assertInGraveyard(player1, "Myr Servitor");
        gd.playerBattlefields.get(player1.getId()).remove(hub);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.assertNotInGraveyard(player1, "Myr Servitor");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Myr Servitor"))
                .hasSize(2);
    }

    @Test
    @DisplayName("A face-down Eon Hub does not skip upkeep")
    void faceDownEonHubDoesNotSkipUpkeep() {
        var hub = harness.addToBattlefieldAndReturn(player1, new EonHub());
        hub.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addToBattlefield(player1, new MyrServitor());
        harness.setGraveyard(player1, List.of(new MyrServitor()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.assertNotInGraveyard(player1, "Myr Servitor");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Myr Servitor"))
                .hasSize(2);
    }
}
