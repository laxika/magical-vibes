package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TreasureCruiseTest extends BaseCardTest {

    @Test
    @DisplayName("Delve pays the generic cost and draws three cards")
    void delvesAndDrawsThreeCards() {
        Card first = new GrizzlyBears();
        Card second = new Shock();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));
        List<Card> graveyard = List.of(
                new Shock(), new GrizzlyBears(), new Shock(), new GrizzlyBears(), new Shock());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new TreasureCruise()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, null, List.of(0, 1, 2, 3, 4));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        harness.assertInGraveyard(player1, "Treasure Cruise");
    }
}
