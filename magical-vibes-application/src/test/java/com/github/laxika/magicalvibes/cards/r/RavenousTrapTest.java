package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RavenousTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target player's graveyard")
    void exilesTargetPlayersGraveyard() {
        Card ownCard = new GrizzlyBears();
        Card targetCard = new AngelsMercy();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(targetCard));
        harness.setHand(player1, List.of(new RavenousTrap()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(targetCard.getId());
        harness.assertInGraveyard(player1, "Ravenous Trap");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(ownCard.getId());
    }

    @Test
    @DisplayName("Can be cast for no mana after an opponent put three cards into their graveyard this turn")
    void castsForFreeAfterOpponentPutThreeCardsIntoGraveyard() {
        Card first = new GrizzlyBears();
        Card second = new AngelsMercy();
        Card third = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(first, second, third));
        gd.cardsPutIntoGraveyardFromAnywhereThisTurn.put(
                player2.getId(), Set.of(first.getId(), second.getId(), third.getId()));
        harness.setHand(player1, List.of(new RavenousTrap()));

        harness.castWithAlternateCost(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot use the alternate cost for cards that were already in an opponent's graveyard")
    void alternateCostRequiresCardsPutIntoGraveyardThisTurn() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new AngelsMercy(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new RavenousTrap()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
