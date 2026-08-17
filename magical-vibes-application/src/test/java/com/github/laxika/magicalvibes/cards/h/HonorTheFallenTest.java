package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HonorTheFallenTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles creature cards from all graveyards and gains one life per card")
    void exilesCreaturesFromAllGraveyardsAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears(), new Shock())));
        harness.setGraveyard(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        harness.setHand(player1, List.of(new HonorTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player2, "Peek");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Does not exile noncreature cards or gain life when no creatures are present")
    void leavesNoncreaturesAndGainsNoLife() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, new ArrayList<>(List.of(new Shock())));
        harness.setGraveyard(player2, new ArrayList<>(List.of(new Peek())));
        harness.setHand(player1, List.of(new HonorTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }
}
