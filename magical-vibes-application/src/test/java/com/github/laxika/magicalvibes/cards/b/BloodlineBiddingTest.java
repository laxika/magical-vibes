package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloodlineBiddingTest extends BaseCardTest {

    private void castAndChoose(String creatureType) {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HillGiant(), new AvianChangeling()));
        harness.setHand(player1, List.of(new BloodlineBidding()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, creatureType);
    }

    @Test
    @DisplayName("Returns every creature card of the chosen type from your graveyard")
    void returnsEveryCreatureCardOfChosenType() {
        castAndChoose("BEAR");

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player1, "Hill Giant")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Hill Giant", "Bloodline Bidding");
    }

    @Test
    @DisplayName("Changeling creature cards match the chosen type")
    void changelingMatchesChosenType() {
        castAndChoose("BEAR");

        assertThat(findPermanents(player1, "Avian Changeling")).hasSize(1);
    }

    @Test
    @DisplayName("Creature cards in an opponent's graveyard are not returned")
    void ignoresOpponentsGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new BloodlineBidding()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new BloodlineBidding()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }
}
