package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelOfFinality.class, GrizzlyBears.class, Shock.class})
class AngelOfFinalityTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles target player's entire graveyard")
    void etbExilesTargetPlayersGraveyard() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setGraveyard(player2, List.of(bears, shock));

        castAngelOfFinality();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(bears.getId(), shock.getId());
    }

    @Test
    @DisplayName("ETB can target and exile its controller's graveyard")
    void etbCanTargetOwnGraveyard() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        castAngelOfFinality();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .contains(bears.getId());
    }

    @Test
    @DisplayName("ETB can target an empty graveyard and leaves the other graveyard alone")
    void etbCanTargetEmptyGraveyard() {
        Card ownCard = new AngelOfFinality();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of());

        castAngelOfFinality();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Angel of Finality");
    }

    @Test
    @DisplayName("ETB exiles cards added to the targeted graveyard before resolution")
    void etbUsesGraveyardContentsAtResolution() {
        Card ownCard = new AngelOfFinality();
        Card originalCard = new AngelOfFinality();
        Card laterCard = new AngelOfFinality();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(originalCard));

        castAngelOfFinality();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(originalCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.setGraveyard(player2, List.of(originalCard, laterCard));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(originalCard.getId(), laterCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void castAngelOfFinality() {
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new AngelOfFinality(), "{3}{W}");
    }
}
