package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpringMind.class, Plains.class, Forest.class, Island.class, Colossapede.class})
class SpringMindTest extends BaseCardTest {

    @Test
    @DisplayName("Spring offers only basic lands to battlefield tapped")
    void springOffersBasicLandsToBattlefieldTapped() {
        harness.setHand(player1, List.of(new SpringMind()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        setupLibrary();

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(3);
        assertThat(search.params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Spring puts chosen basic land onto the battlefield tapped")
    void springPutsBasicLandOntoBattlefieldTapped() {
        harness.setHand(player1, List.of(new SpringMind()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        setupLibrary();

        harness.castAndResolveSorcery(player1, 0, List.of());

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
        harness.assertInGraveyard(player1, "Spring");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Mind draws two cards from graveyard then exiles")
    void mindDrawsTwoThenExiles() {
        harness.setLibrary(player1, List.of(new Island(), new Forest(), new Plains()));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new SpringMind()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Spring") || c.getName().equals("Mind"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spring"));
    }

    @Test
    @DisplayName("Mind can be cast at instant speed")
    void mindCastableAtInstantSpeed() {
        harness.setLibrary(player1, List.of(new Island(), new Forest()));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new SpringMind()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spring"));
    }

    @Test
    @DisplayName("Spring may fail to find even when basic lands are available")
    void springMayDeclineSearch() {
        harness.setHand(player1, List.of(new SpringMind()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        setupLibrary();
        List<Card> originalLibrary = List.copyOf(gd.playerDecks.get(player1.getId()));

        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(originalLibrary);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Spring");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Spring resolves when the library contains no basic lands")
    void springWithNoBasicLands() {
        Colossapede nonland = new Colossapede();
        harness.setLibrary(player1, List.of(nonland));
        harness.setHand(player1, List.of(new SpringMind()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Spring");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Mind can be cast during an opponent's turn")
    void mindDuringOpponentsTurn() {
        harness.setLibrary(player1, List.of(new Island(), new Forest(), new Plains()));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new SpringMind()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        gd.activePlayerId = player2.getId();
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spring"));
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new Colossapede()));
    }
}
