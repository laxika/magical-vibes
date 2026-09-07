package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BonehoardDracosaur.class, Forest.class, GrizzlyBears.class})
class BonehoardDracosaurTest extends BaseCardTest {

    @Test
    @DisplayName("Two exiled lands create one Dinosaur token")
    void twoLandsCreateOneDinosaur() {
        List<Card> exiled = resolveWithLibrary(new Forest(), new Forest());

        assertThat(findPermanents(player1, "Dinosaur")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(exiled).hasSize(2);
        assertThat(exiled).allSatisfy(card -> assertThat(gd.exilePlayPermissions)
                .containsEntry(card.getId(), player1.getId()));

        Permanent dinosaur = findPermanent(player1, "Dinosaur");
        assertThat(dinosaur.getCard().getSubtypes()).contains(CardSubtype.DINOSAUR);
        assertThat(gqs.getEffectivePower(gd, dinosaur)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dinosaur)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two exiled nonlands create one Treasure token")
    void twoNonlandsCreateOneTreasure() {
        List<Card> exiled = resolveWithLibrary(new GrizzlyBears(), new GrizzlyBears());

        assertThat(findPermanents(player1, "Dinosaur")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(exiled).hasSize(2);
    }

    @Test
    @DisplayName("A land and a nonland create one Dinosaur and one Treasure")
    void mixedCardsCreateBothTokens() {
        resolveWithLibrary(new Forest(), new GrizzlyBears());

        assertThat(findPermanents(player1, "Dinosaur")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    private List<Card> resolveWithLibrary(Card... cards) {
        harness.addToBattlefield(player1, new BonehoardDracosaur());
        harness.setLibrary(player1, List.of(cards));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        return gd.getPlayerExiledCards(player1.getId());
    }
}
