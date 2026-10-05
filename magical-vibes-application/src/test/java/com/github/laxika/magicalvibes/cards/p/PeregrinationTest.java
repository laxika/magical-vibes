package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TempleOfPlenty;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Peregrination.class, Plains.class, Forest.class, Island.class, GrizzlyBears.class, TempleOfPlenty.class})
class PeregrinationTest extends BaseCardTest {

    @Test
    @DisplayName("Peregrination searches for two basic lands, then scries 1")
    void searchesAndScries() {
        Card plains = new Plains();
        Card forest = new Forest();
        Card island = new Island();
        Card nonBasic = new GrizzlyBears();
        setupAndCast(List.of(plains, forest, island, nonBasic));

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .hasSize(3)
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == plains && permanent.isTapped());
        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(1)
                .containsAnyOf(island, nonBasic);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(nonBasic, island);
    }

    @Test
    @DisplayName("Peregrination still scries when no basic land is found")
    void scriesWhenNoBasicLandIsFound() {
        Card topCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
        setupAndCast(List.of(topCard, secondCard));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(1)
                .containsAnyOf(topCard, secondCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Finding only one available land puts it onto the battlefield tapped before scrying")
    void findsOnlyAvailableLand() {
        Card land = new Forest();
        Card remaining = new GrizzlyBears();
        setupAndCast(List.of(land, remaining));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == land && permanent.isTapped());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(remaining);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof Peregrination);
    }

    @Test
    @DisplayName("The controller may find one land even when another basic land is available")
    void declinesSecondLand() {
        Card battlefieldLand = new Plains();
        Card unchosenLand = new Forest();
        setupAndCast(List.of(battlefieldLand, unchosenLand));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == battlefieldLand && permanent.isTapped());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(unchosenLand);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unchosenLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Finding zero lands skips both destinations and still scries")
    void declinesAllLands() {
        Card firstLand = new Plains();
        Card secondLand = new Forest();
        setupAndCast(List.of(firstLand, secondLand));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        Card scryCard = gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards().getFirst();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(firstLand, secondLand);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(scryCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library does not prevent Peregrination from finishing resolution")
    void resolvesWithEmptyLibrary() {
        setupAndCast(List.of());

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof Peregrination);
    }

    @Test
    @DisplayName("Nonbasic lands cannot be found, and the scryed card can go to the bottom")
    void excludesNonbasicLandsAndScriesToBottom() {
        Card nonbasicLand = new TempleOfPlenty();
        Card otherCard = new GrizzlyBears();
        setupAndCast(List.of(nonbasicLand, otherCard));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        List<Card> scryCards = gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards();
        assertThat(scryCards).hasSize(1);
        Card scryCard = scryCards.getFirst();
        Card unscryedCard = scryCard == nonbasicLand ? otherCard : nonbasicLand;

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unscryedCard, scryCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupAndCast(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new Peregrination(), "{3}{G}");
    }
}
