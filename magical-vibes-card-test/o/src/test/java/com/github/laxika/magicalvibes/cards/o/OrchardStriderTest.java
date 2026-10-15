package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrchardStrider.class, Forest.class, GrizzlyBears.class})
class OrchardStriderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates two Food artifact tokens")
    void etbCreatesTwoFoodTokens() {
        harness.setHand(player1, List.of(new OrchardStrider()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> foodTokens = findPermanents(player1, "Food");
        assertThat(foodTokens).hasSize(2);
        assertThat(foodTokens).allSatisfy(food -> {
            assertThat(food.getCard().getType()).isEqualTo(CardType.ARTIFACT);
            assertThat(food.getCard().getSubtypes()).contains(CardSubtype.FOOD);
            assertThat(food.getCard().isToken()).isTrue();
        });
    }

    @Test
    @DisplayName("Basic landcycling discards the card and searches for a basic land")
    void basicLandcyclingSearchesForBasicLand() {
        harness.setHand(player1, List.of(new OrchardStrider()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Orchard Strider");
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).singleElement().satisfies(card ->
                assertThat(card).isInstanceOf(Forest.class));

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A newly created Food can be sacrificed for three life")
    void foodCanBeActivatedImmediately() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new OrchardStrider()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent food = findPermanent(player1, "Food");
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, foodIndex, null, null);

        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(food);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Basic landcycling pays its discard cost before resolution and creates no Food")
    void basicLandcyclingWithNoBasicLand() {
        harness.setHand(player1, List.of(new OrchardStrider()));
        harness.setLibrary(player1, List.of(new OrchardStrider()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Orchard Strider");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Basic landcycling may fail to find even when a basic land is available")
    void basicLandcyclingMayFailToFind() {
        harness.setHand(player1, List.of(new OrchardStrider()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Orchard Strider");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Basic landcycling requires green mana and does not discard when the cost cannot be paid")
    void basicLandcyclingRequiresGreenMana() {
        harness.setHand(player1, List.of(new OrchardStrider()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Orchard Strider");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
