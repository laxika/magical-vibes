package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RuthlessRadrat.class, GrizzlyBears.class})
class RuthlessRadratTest extends BaseCardTest {

    @Test
    @DisplayName("Squad exiles four graveyard cards per payment and creates matching token copies")
    void squadExilesCardsAndCreatesCopies() {
        List<Card> graveyard = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new RuthlessRadrat()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreatureWithRepeatedCostsAndGraveyardExile(
                player1, 0, List.of("{0}", "{0}"), List.of(0, 1, 2, 3, 4, 5, 6, 7));
        harness.passBothPriorities();
        resolveAllTriggers();

        List<Permanent> radrats = findPermanents(player1, "Ruthless Radrat");
        assertThat(radrats).hasSize(3);
        assertThat(radrats).filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(8);
    }

    @Test
    @DisplayName("Squad is optional")
    void squadMayBePaidZeroTimes() {
        harness.setHand(player1, List.of(new RuthlessRadrat()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Ruthless Radrat")).hasSize(1);
    }

    @Test
    @DisplayName("Squad rejects a payment without enough graveyard cards")
    void squadRequiresFourCardsPerPayment() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new RuthlessRadrat()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreatureWithRepeatedCostsAndGraveyardExile(
                player1, 0, List.of("{0}"), List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must exile exactly 4 cards");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }
}
