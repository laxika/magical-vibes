package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JeweledBirdTest extends BaseCardTest {

    private void birdReady() {
        harness.addToBattlefield(player1, new JeweledBird());
        harness.setLibrary(player1, List.of(new HillGiant()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Anting Jeweled Bird puts your other anted cards into your graveyard and draws")
    void antesOtherCardsAndDraws() {
        gd.addToAnte(player1.getId(), new GrizzlyBears());
        gd.addToExile(player1.getId(), new AirElemental());
        birdReady();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Jeweled Bird", "Air Elemental");
    }

    @Test
    @DisplayName("Anting Jeweled Bird does not move ordinary exiled cards")
    void leavesOrdinaryExileAlone() {
        gd.addToExile(player1.getId(), new AirElemental());
        birdReady();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Jeweled Bird", "Air Elemental");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
