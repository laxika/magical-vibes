package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DawnhartRejuvenator;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PathToTheFestival.class, Forest.class, DawnhartRejuvenator.class, Island.class, Mountain.class})
class PathToTheFestivalTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a basic land onto the battlefield tapped and scries when Domain is met")
    void searchesAndScriesWithThreeBasicLandTypes() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.setLibrary(player1, List.of(new Mountain(), new DawnhartRejuvenator()));
        castPathToTheFestival();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        harness.handleCardChosen(player1, 0);

        Permanent mountain = findPermanent(player1, "Mountain");
        assertThat(mountain.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
    }

    @Test
    @DisplayName("Does not scry when fewer than three basic land types are controlled after the search")
    void doesNotScryWithFewerThanThreeBasicLandTypes() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.setLibrary(player1, List.of(new Forest(), new DawnhartRejuvenator()));
        castPathToTheFestival();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can fail to find a basic land and skips the Domain scry when below three types")
    void failingToFindSkipsScryBelowThreeTypes() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.setLibrary(player1, List.of(new DawnhartRejuvenator()));
        castPathToTheFestival();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castPathToTheFestival() {
        harness.setHand(player1, List.of(new PathToTheFestival()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Still scries after declining to find a land with three land types already controlled")
    void decliningSearchStillScriesWithThreeTypes() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.setLibrary(player1, List.of(new Forest(), new DawnhartRejuvenator()));
        castPathToTheFestival();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);
        Card viewedCard = scry.cards().getFirst();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2).last().isSameAs(viewedCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Still scries when no basic land can be found and three types are already controlled")
    void noMatchingLandStillScriesWithThreeTypes() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        Card remainingCard = new DawnhartRejuvenator();
        harness.setLibrary(player1, List.of(remainingCard));
        castPathToTheFestival();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(remainingCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(PathToTheFestival.class::isInstance);
    }

    @Test
    @DisplayName("Opponent's land types do not count toward the scry condition")
    void opponentsLandTypesDoNotCount() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Mountain());
        harness.setLibrary(player1, List.of(new Forest(), new DawnhartRejuvenator()));
        castPathToTheFestival();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Forest")).hasSize(2);
    }

    @Test
    @DisplayName("Flashback searches for one tapped land, scries, and exiles the spell")
    void flashbackResolvesAllEffectsAndExilesSpell() {
        PathToTheFestival spell = new PathToTheFestival();
        harness.setGraveyard(player1, List.of(spell));
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        Card remainingCard = new DawnhartRejuvenator();
        harness.setLibrary(player1, List.of(new Mountain(), remainingCard));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Mountain").isTapped()).isTrue();
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(remainingCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }
}
