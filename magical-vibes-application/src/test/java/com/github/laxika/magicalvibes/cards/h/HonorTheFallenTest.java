package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.Brainstorm;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HonorTheFallen.class, FreshVolunteers.class, Brainstorm.class})
class HonorTheFallenTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles creature cards from all graveyards and gains one life per card")
    void exilesCreaturesFromAllGraveyardsAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(new FreshVolunteers(), new Brainstorm()));
        harness.setGraveyard(player2, List.of(new FreshVolunteers(), new Brainstorm()));
        harness.castFromHand(player1, new HonorTheFallen(), "{1}{W}");

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Brainstorm");
        harness.assertInGraveyard(player2, "Brainstorm");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Does not exile noncreature cards or gain life when no creatures are present")
    void leavesNoncreaturesAndGainsNoLife() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(new Brainstorm()));
        harness.setGraveyard(player2, List.of(new Brainstorm()));
        harness.castFromHand(player1, new HonorTheFallen(), "{1}{W}");

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }
}
