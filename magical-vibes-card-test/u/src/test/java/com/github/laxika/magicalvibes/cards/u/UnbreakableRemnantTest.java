package com.github.laxika.magicalvibes.cards.u;

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

@CardUsed({UnbreakableRemnant.class, GrizzlyBears.class})
class UnbreakableRemnantTest extends BaseCardTest {

    @Test
    @DisplayName("Escape exiles two other graveyard cards and perpetually boosts all owned copies")
    void escapeExilesCardsAndPerpetuallyBoostsOwnedCopies() {
        UnbreakableRemnant escapedCard = new UnbreakableRemnant();
        UnbreakableRemnant handCard = new UnbreakableRemnant();
        escapedCard.setOwnerId(player1.getId());
        handCard.setOwnerId(player1.getId());
        Card firstExiledCard = new GrizzlyBears();
        Card secondExiledCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(escapedCard, firstExiledCard, secondExiledCard));
        harness.setHand(player1, List.of(handCard));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromGraveyard(player1, 0, List.of(1, 2));
        resolveAllTriggers();

        Permanent escaped = findPermanent(player1, "Unbreakable Remnant");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(firstExiledCard, secondExiledCard);
        assertThat(gqs.getEffectivePower(gd, escaped)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, escaped)).isEqualTo(2);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Unbreakable Remnant"))
                .allSatisfy(permanent -> {
                    assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(3);
                    assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(2);
                });
    }

    @Test
    @DisplayName("Normal casts do not get the graveyard-cast perpetual boost")
    void normalCastDoesNotTriggerBoost() {
        harness.castFromHand(player1, new UnbreakableRemnant(), "{W}");
        resolveAllTriggers();

        Permanent remnant = findPermanent(player1, "Unbreakable Remnant");
        assertThat(gqs.getEffectivePower(gd, remnant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, remnant)).isEqualTo(1);
    }

    @Test
    @DisplayName("Escape requires two other cards in the graveyard")
    void escapeRequiresTwoOtherCards() {
        harness.setGraveyard(player1, List.of(new UnbreakableRemnant(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exactly 2");
    }

    @Test
    @DisplayName("The cast trigger boosts owned copies across zones and under another player's control")
    void boostsOwnedCopiesAcrossZonesBeforeSpellResolves() {
        UnbreakableRemnant escapedCard = new UnbreakableRemnant();
        escapedCard.setOwnerId(player1.getId());
        UnbreakableRemnant libraryCard = new UnbreakableRemnant();
        UnbreakableRemnant graveyardCard = new UnbreakableRemnant();
        UnbreakableRemnant exiledCard = new UnbreakableRemnant();
        UnbreakableRemnant stolenCard = new UnbreakableRemnant();
        stolenCard.setOwnerId(player1.getId());
        UnbreakableRemnant opposingCard = new UnbreakableRemnant();
        opposingCard.setOwnerId(player2.getId());
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, stolenCard);
        Permanent opposing = harness.addToBattlefieldAndReturn(player1, opposingCard);
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of(escapedCard, exiledCard,
                new UnbreakableRemnant(), graveyardCard));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromGraveyard(player1, 0, List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        for (Card card : List.of(escapedCard, libraryCard, graveyardCard, exiledCard)) {
            assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(3);
            assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(2);
        }
        assertThat(gqs.getEffectivePower(gd, stolen)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stolen)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(1);

        resolveAllTriggers();
        assertThat(findPermanents(player1, "Unbreakable Remnant"))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(escapedCard.getId());
                    assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(3);
                    assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(2);
                });
    }

    @Test
    @DisplayName("Successive graveyard casts accumulate perpetual boosts")
    void successiveEscapesAccumulateBoosts() {
        UnbreakableRemnant first = new UnbreakableRemnant();
        UnbreakableRemnant second = new UnbreakableRemnant();
        first.setOwnerId(player1.getId());
        second.setOwnerId(player1.getId());
        harness.setGraveyard(player1, List.of(first, new UnbreakableRemnant(),
                new UnbreakableRemnant(), second, new UnbreakableRemnant(),
                new UnbreakableRemnant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromGraveyard(player1, 0, List.of(1, 2));
        resolveAllTriggers();
        harness.castFromGraveyard(player1, 0, List.of(1, 2));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Unbreakable Remnant"))
                .hasSize(2)
                .allSatisfy(permanent -> {
                    assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(4);
                    assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(3);
                });
    }
}
