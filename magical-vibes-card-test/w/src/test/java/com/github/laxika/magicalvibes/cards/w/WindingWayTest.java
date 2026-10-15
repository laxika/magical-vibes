package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WindingWay.class, Forest.class, GrizzlyBears.class, Island.class, Shock.class})
class WindingWayTest extends BaseCardTest {

    @Test
    void offersOnlyCreatureAndLandChoices() {
        cast(List.of(new GrizzlyBears(), new Forest(), new Shock(), new Island()));

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly(CardType.CREATURE.name(), CardType.LAND.name());
    }

    @Test
    void choosingCreaturePutsCreaturesIntoHandAndTheRestIntoGraveyard() {
        Card creature1 = new GrizzlyBears();
        Card land = new Forest();
        Card instant = new Shock();
        Card creature2 = new GrizzlyBears();

        cast(List.of(creature1, land, instant, creature2));
        harness.handleListChoice(player1, CardType.CREATURE.name());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature1, creature2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land, instant);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void choosingLandPutsLandsIntoHandAndTheRestIntoGraveyard() {
        Card creature = new GrizzlyBears();
        Card land1 = new Forest();
        Card instant = new Shock();
        Card land2 = new Island();

        cast(List.of(creature, land1, instant, land2));
        harness.handleListChoice(player1, CardType.LAND.name());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land1, land2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature, instant);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void leavesCardsBelowTheTopFourUntouched() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card instant = new Shock();
        Card secondLand = new Island();
        Card fifth = new GrizzlyBears();
        Card sixth = new Forest();

        cast(List.of(creature, land, instant, secondLand, fifth, sixth));
        harness.handleListChoice(player1, CardType.CREATURE.name());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land, instant, secondLand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifth, sixth);
    }

    @Test
    void revealsAllAvailableCardsWhenLibraryHasFewerThanFour() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();

        cast(List.of(creature, land));
        harness.handleListChoice(player1, CardType.LAND.name());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void putsEveryRevealedCardIntoGraveyardWhenNoneMatch() {
        Card land = new Forest();
        Card instant = new Shock();
        Card secondLand = new Island();
        Card secondInstant = new Shock();

        cast(List.of(land, instant, secondLand, secondInstant));
        harness.handleListChoice(player1, CardType.CREATURE.name());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land, instant, secondLand, secondInstant);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void putsAllMatchingCardsIntoHand() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Card fourth = new GrizzlyBears();

        cast(List.of(first, second, third, fourth));
        harness.handleListChoice(player1, CardType.CREATURE.name());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third, fourth);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void resolvesWithAnEmptyLibraryWithoutDrawing() {
        cast(List.of());
        harness.handleListChoice(player1, CardType.LAND.name());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    private void cast(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new WindingWay(), "{1}{G}");
        harness.passBothPriorities();
    }
}
