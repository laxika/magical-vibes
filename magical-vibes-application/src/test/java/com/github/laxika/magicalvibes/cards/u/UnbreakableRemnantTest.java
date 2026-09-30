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
        Card firstExiledCard = new GrizzlyBears();
        Card secondExiledCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(escapedCard, firstExiledCard, secondExiledCard));
        harness.setHand(player1, List.of(handCard));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromGraveyard(player1, 0, List.of(1, 2));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent escaped = findPermanent(player1, "Unbreakable Remnant");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(firstExiledCard, secondExiledCard);
        assertThat(gqs.getEffectivePower(gd, escaped)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, escaped)).isEqualTo(2);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Unbreakable Remnant"))
                .allSatisfy(permanent -> {
                    assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(3);
                    assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(2);
                });
    }

    @Test
    @DisplayName("Normal casts do not get the graveyard-cast perpetual boost")
    void normalCastDoesNotTriggerBoost() {
        harness.setHand(player1, List.of(new UnbreakableRemnant()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

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
}
