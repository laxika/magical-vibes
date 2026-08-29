package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AngelOfFinalityTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles target player's entire graveyard")
    void etbExilesTargetPlayersGraveyard() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears, shock)));

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
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));

        castAngelOfFinality();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .contains(bears.getId());
    }

    private void castAngelOfFinality() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new AngelOfFinality()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
    }
}
