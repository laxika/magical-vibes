package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DesecratedTomb;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeasonsPast.class, GrizzlyBears.class, GiantSpider.class, LlanowarElves.class,
        HolyDay.class, Forest.class, DesecratedTomb.class})
class SeasonsPastTest extends BaseCardTest {

    @Test
    @DisplayName("Returns selected cards with different mana values and goes to the bottom of its owner's library")
    void returnsCardsWithDifferentManaValues() {
        Card bears = new GrizzlyBears();
        Card spider = new GiantSpider();
        Card elves = new LlanowarElves();
        Card holyDay = new HolyDay();
        Card seasonsPast = new SeasonsPast();
        harness.setGraveyard(player1, List.of(bears, spider, elves, holyDay));
        harness.setHand(player1, List.of(seasonsPast));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        harness.handleGraveyardCardChosen(player1, 2);

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.validIndices()).containsExactly(0, 1);

        harness.handleGraveyardCardChosen(player1, 0);
        choice = gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Giant Spider");
        harness.assertInHand(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Holy Day");
        assertThat(gd.playerDecks.get(player1.getId()).getLast().getId()).isEqualTo(seasonsPast.getId());
    }

    @Test
    @DisplayName("May return zero cards")
    void mayReturnZeroCards() {
        Card card = new GrizzlyBears();
        Card seasonsPast = new SeasonsPast();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of(seasonsPast));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleGraveyardCardChosen(player1, -1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()).getLast().getId()).isEqualTo(seasonsPast.getId());
    }

    @Test
    void emptyGraveyardStillPutsSpellOnBottom() {
        Card seasonsPast = new SeasonsPast();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(seasonsPast));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, seasonsPast);
        harness.assertNotInGraveyard(player1, "Seasons Past");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayStopAfterReturningOnlyOneCard() {
        Card seasonsPast = new SeasonsPast();
        Card bears = new GrizzlyBears();
        Card spider = new GiantSpider();
        harness.setGraveyard(player1, List.of(bears, spider));
        harness.setHand(player1, List.of(seasonsPast));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, -1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spider);
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(seasonsPast);
    }

    @Test
    void canReturnOneLandAndLeavesOpponentsGraveyardAlone() {
        Card firstForest = new Forest();
        Card secondForest = new Forest();
        Card elves = new LlanowarElves();
        Card opponentsCard = new GiantSpider();
        harness.setGraveyard(player1, List.of(firstForest, secondForest, elves));
        harness.setGraveyard(player2, List.of(opponentsCard));
        harness.setHand(player1, List.of(new SeasonsPast()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleGraveyardCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)
                .validIndices()).containsExactly(1);
        harness.handleGraveyardCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstForest, elves);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(secondForest);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCard);
    }

    @Test
    void returningSeveralCreaturesTriggersDesecratedTombOnlyOnce() {
        harness.addToBattlefield(player1, new DesecratedTomb());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GiantSpider()));
        harness.setHand(player1, List.of(new SeasonsPast()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, 0);
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Giant Spider");
        assertThat(findPermanents(player1, "Bat")).hasSize(1);
    }
}
