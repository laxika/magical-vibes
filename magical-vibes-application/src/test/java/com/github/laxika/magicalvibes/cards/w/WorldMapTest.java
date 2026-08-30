package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WorldMap.class, EvolvingWilds.class, Forest.class, GrizzlyBears.class, Plains.class})
class WorldMapTest extends BaseCardTest {

    @Test
    @DisplayName("The one-mana ability sacrifices World Map and searches for a basic land")
    void searchesForBasicLand() {
        addMapAndMana(1);
        setLibrary(new Plains(), new Forest(), new EvolvingWilds(), new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .hasSize(2)
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        harness.assertInGraveyard(player1, "World Map");
    }

    @Test
    @DisplayName("The three-mana ability searches for any land")
    void searchesForAnyLand() {
        addMapAndMana(3);
        setLibrary(new Plains(), new EvolvingWilds(), new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Plains", "Evolving Wilds");
        harness.assertInGraveyard(player1, "World Map");
    }

    @Test
    @DisplayName("Choosing a searched land puts it into hand")
    void chosenLandEntersHand() {
        addMapAndMana(1);
        setLibrary(new Plains(), new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Plains"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addMapAndMana(int amount) {
        harness.addToBattlefield(player1, new WorldMap());
        harness.addMana(player1, ManaColor.COLORLESS, amount);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
