package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SouthernAirTemple;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KyoshiIslandPlaza.class, KyoshiVillage.class, Forest.class, Island.class, Plains.class, SouthernAirTemple.class})
class KyoshiIslandPlazaTest extends BaseCardTest {

    @Test
    void entersAndSearchesForUpToTheNumberOfShrinesYouControl() {
        harness.addToBattlefield(player1, shrine());
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island()));

        harness.enterBattlefieldAndReturn(player1, new KyoshiIslandPlaza());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(3);
        assertThat(search.params().remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(2)
                .allSatisfy(permanent -> assertThat(permanent.isTapped()).isTrue());
    }

    @Test
    void anotherShrineEnteringSearchesForOneBasicLand() {
        harness.addToBattlefield(player1, new KyoshiIslandPlaza());
        harness.setLibrary(player1, List.of(new Plains(), new Forest()));

        harness.enterBattlefieldAndReturn(player1, shrine());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(2);
        assertThat(search.params().remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(1)
                .allSatisfy(permanent -> assertThat(permanent.isTapped()).isTrue());
    }

    @Test
    void nonShrineEnchantmentEnteringDoesNotTriggerTheSearch() {
        harness.addToBattlefield(player1, new KyoshiIslandPlaza());
        harness.setLibrary(player1, List.of(new Plains()));

        harness.enterBattlefieldAndReturn(player1, enchantment());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringAloneCountsItselfButDoesNotTriggerItsSecondAbility() {
        harness.setLibrary(player1, List.of(new Forest(), new KyoshiIslandPlaza(), new KyoshiVillage()));

        harness.enterBattlefieldAndReturn(player1, new KyoshiIslandPlaza());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().remainingCount()).isEqualTo(1);
        assertThat(search.params().cards()).hasSize(1);

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void entrySearchCountsShrinesAtResolutionAndIgnoresOpponentsShrines() {
        harness.addToBattlefield(player2, new SouthernAirTemple());
        harness.setLibrary(player1, List.of(new Forest(), new Plains(), new Island()));
        harness.enterBattlefieldAndReturn(player1, new KyoshiIslandPlaza());

        harness.addToBattlefield(player1, new SouthernAirTemple());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().remainingCount()).isEqualTo(2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(1)
                .allSatisfy(permanent -> assertThat(permanent.isTapped()).isTrue());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void entrySearchCanFindNoCardsEvenWhenBasicLandsAreAvailable() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.enterBattlefieldAndReturn(player1, new KyoshiIslandPlaza());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void anotherShrineSearchFiltersNonbasicLandsAndCanFailToFind() {
        harness.addToBattlefield(player1, new KyoshiIslandPlaza());
        harness.setLibrary(player1, List.of(new KyoshiVillage(), new Forest()));

        harness.enterBattlefieldAndReturn(player1, new SouthernAirTemple());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() == null) {
            harness.passBothPriorities();
        }

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().remainingCount()).isEqualTo(1);
        assertThat(search.params().cards()).hasSize(1);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(CardType.LAND));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opponentsShrineEnteringDoesNotTriggerYourPlaza() {
        harness.addToBattlefield(player1, new KyoshiIslandPlaza());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.enterBattlefieldAndReturn(player2, new SouthernAirTemple());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private Card shrine() {
        Card card = new Card();
        card.setName("Test Shrine");
        card.setType(CardType.ENCHANTMENT);
        card.setSubtypes(List.of(CardSubtype.SHRINE));
        return card;
    }

    private Card enchantment() {
        Card card = new Card();
        card.setName("Test Enchantment");
        card.setType(CardType.ENCHANTMENT);
        return card;
    }
}
