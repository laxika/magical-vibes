package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CorpseAppraiser.class, Forest.class, CivicGardener.class, Murder.class})
class CorpseAppraiserTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles a creature card and looks at the top three cards")
    void exilesCreatureAndChoosesFromTopThree() {
        Card target = new CivicGardener();
        Card kept = new Forest();
        Card graveyardOne = new Murder();
        Card graveyardTwo = new Murder();
        Card fourthCard = new Forest();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player1, List.of(kept, graveyardOne, graveyardTwo, fourthCard));

        castCorpseAppraiser();

        PendingInteraction.MultiGraveyardChoice graveyardChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(graveyardChoice.validCardIds()).containsExactly(target.getId());

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(kept.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        assertThat(gd.playerHands.get(player1.getId())).contains(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardOne, graveyardTwo);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourthCard);
    }

    @Test
    @DisplayName("Choosing no graveyard target skips the library effect")
    void noGraveyardTargetSkipsLibraryEffect() {
        Card target = new CivicGardener();
        Card topCard = new Forest();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player1, List.of(topCard));

        castCorpseAppraiser();

        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("ETB does not offer noncreature graveyard cards")
    void noncreatureIsNotTargetable() {
        Card target = new Murder();
        harness.setGraveyard(player2, List.of(target));

        castCorpseAppraiser();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
    }

    @Test
    @DisplayName("A successful exile requires putting one looked-at card into hand")
    void cannotDeclineCardToHandAfterExiling() {
        Card target = new CivicGardener();
        Card kept = new Forest();
        Card rest = new Murder();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player1, List.of(kept, rest));

        castCorpseAppraiser();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(kept.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(rest);
    }

    @Test
    @DisplayName("The controller can exile a creature from their own graveyard and choose from two cards")
    void ownGraveyardAndTwoCardLibrary() {
        Card target = new CivicGardener();
        Card kept = new Murder();
        Card rest = new Forest();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(rest, kept));

        castCorpseAppraiser();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(kept.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(rest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An ability whose target leaves the graveyard does not look at the library")
    void departedTargetSkipsLibraryEffect() {
        Card target = new CivicGardener();
        Card top = new Forest();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player1, List.of(top));

        castCorpseAppraiser();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(target));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
    }

    @Test
    @DisplayName("With one library card the ability puts that card into hand")
    void oneCardLibrary() {
        Card target = new CivicGardener();
        Card top = new Forest();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player1, List.of(top));

        castCorpseAppraiser();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty library does not prevent the creature card from being exiled")
    void emptyLibraryStillExiles() {
        Card target = new CivicGardener();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player1, List.of());

        castCorpseAppraiser();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void castCorpseAppraiser() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new CorpseAppraiser(), "{U}{B}{R}");
        harness.passBothPriorities();
    }
}
