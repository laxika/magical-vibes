package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BotanicalSanctum;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildWanderer.class, Plains.class, Island.class, BotanicalSanctum.class})
class WildWandererTest extends BaseCardTest {

    @Test
    void etbMaySearchesForABasicLandToTheBattlefieldTapped() {
        harness.castFromHand(player1, new WildWanderer(), "{3}{G}");

        harness.setLibrary(player1, List.of(new Plains(), new Island(), new WildWanderer()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gameData = harness.getGameData();
        PendingInteraction.LibrarySearch search = gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND) && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        int battlefieldBefore = gameData.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gameData.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.LAND) && permanent.isTapped());
    }

    @Test
    void decliningSearchLeavesLibraryUnchanged() {
        List<Card> library = List.of(new Plains(), new Island(), new WildWanderer());
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new WildWanderer(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void acceptedSearchCanFailToFindEvenWithBasicLandAvailable() {
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(plains));
        harness.castFromHand(player1, new WildWanderer(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void acceptedSearchWithNoBasicLandsFinishesWithoutMovingCards() {
        WildWanderer otherWanderer = new WildWanderer();
        harness.setLibrary(player1, List.of(otherWanderer));
        harness.castFromHand(player1, new WildWanderer(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherWanderer);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void acceptedSearchWithEmptyLibraryFinishesNormally() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new WildWanderer(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    void searchExcludesNonbasicLandsAndUsesOnlyControllersLibrary() {
        Plains plains = new Plains();
        BotanicalSanctum sanctum = new BotanicalSanctum();
        Island opponentsIsland = new Island();
        harness.setLibrary(player1, List.of(sanctum, plains, new WildWanderer()));
        harness.setLibrary(player2, List.of(opponentsIsland));
        harness.castFromHand(player1, new WildWanderer(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(plains);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == plains && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).contains(sanctum).doesNotContain(plains);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsIsland);
    }
}
