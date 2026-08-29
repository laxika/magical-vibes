package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Geralf's Masterpiece")
class GeralfsMasterpieceTest extends BaseCardTest {

    @Test
    @DisplayName("Gets -1/-1 for each card in its controller's hand")
    void getsMinusOneForEachCardInHand() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new Mountain()));
        harness.addToBattlefield(player1, new GeralfsMasterpiece());

        Permanent masterpiece = findPermanent(player1, "Geralf's Masterpiece");

        assertThat(gqs.getEffectivePower(gd, masterpiece)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, masterpiece)).isEqualTo(5);
    }

    @Test
    @DisplayName("Returns tapped from the graveyard after discarding three cards")
    void returnsTappedAfterDiscardingThreeCards() {
        harness.forceActivePlayer(player1);
        harness.setGraveyard(player1, List.of(new GeralfsMasterpiece()));
        harness.setHand(player1, List.of(new GrizzlyBears(), new Mountain(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent masterpiece = findPermanent(player1, "Geralf's Masterpiece");

        assertThat(masterpiece.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, masterpiece)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, masterpiece)).isEqualTo(7);
        harness.assertNotInGraveyard(player1, "Geralf's Masterpiece");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without three cards in hand")
    void cannotActivateWithoutThreeCardsInHand() {
        harness.setGraveyard(player1, List.of(new GeralfsMasterpiece()));
        harness.setHand(player1, List.of(new GrizzlyBears(), new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Geralf's Masterpiece");
    }
}
