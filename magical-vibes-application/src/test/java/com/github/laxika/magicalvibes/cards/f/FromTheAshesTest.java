package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.b.BalefulStrix;
import com.github.laxika.magicalvibes.cards.s.SavageLands;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FromTheAshes.class, SavageLands.class, Forest.class, Island.class, BalefulStrix.class})
class FromTheAshesTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all nonbasic lands and searches once per destroyed land in APNAP order")
    void destroysNonbasicLandsAndSearchesPerDestroyedLand() {
        harness.addToBattlefield(player1, new SavageLands());
        harness.addToBattlefield(player1, new SavageLands());
        harness.addToBattlefield(player2, new SavageLands());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new BalefulStrix());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Island()));

        castFromTheAshes();

        assertThat(activeSearch().params().playerId()).isEqualTo(player1.getId());
        assertThat(activeSearch().params().remainingCount()).isEqualTo(1);
        harness.handleCardChosen(player1, 0);

        assertThat(activeSearch().params().playerId()).isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(3)
                .allMatch(permanent -> !permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(1)
                .allMatch(permanent -> !permanent.isTapped());
        assertThat(gd.gameLog).extracting(GameLogEntry::plainText)
                .filteredOn(log -> log.endsWith("'s library is shuffled."))
                .hasSize(2);
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Baleful Strix");
        harness.assertInGraveyard(player1, "Savage Lands");
        harness.assertInGraveyard(player2, "Savage Lands");
    }

    @Test
    @DisplayName("Each optional search can be declined independently")
    void searchesCanBeDeclinedIndependently() {
        harness.addToBattlefield(player1, new SavageLands());
        harness.addToBattlefield(player1, new SavageLands());
        harness.addToBattlefield(player2, new SavageLands());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Island()));

        castFromTheAshes();

        harness.handleCardChosen(player1, -1);
        assertThat(activeSearch().params().playerId()).isEqualTo(player1.getId());

        harness.handleCardChosen(player1, -1);
        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, -1);
        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND)).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND)).isEmpty();
    }

    @Test
    @DisplayName("Indestructible nonbasic lands do not create a search")
    void indestructibleLandsDoNotCreateSearches() {
        Permanent indestructible = harness.addToBattlefieldAndReturn(player1, new SavageLands());
        indestructible.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.addToBattlefield(player2, new SavageLands());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Island()));

        castFromTheAshes();

        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);
        assertThat(activeSearch()).isNull();
        harness.assertOnBattlefield(player1, "Savage Lands");
        harness.assertInGraveyard(player2, "Savage Lands");
    }

    @Test
    @DisplayName("Declining every search leaves both libraries unshuffled")
    void decliningAllSearchesDoesNotShuffle() {
        harness.addToBattlefield(player1, new SavageLands());
        harness.addToBattlefield(player2, new SavageLands());
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Forest()));

        castFromTheAshes();
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player2, -1);

        assertThat(activeSearch()).isNull();
        assertThat(gd.gameLog).extracting(GameLogEntry::plainText)
                .noneMatch(log -> log.endsWith("'s library is shuffled."));
    }

    @Test
    @DisplayName("A player with no basic lands still chooses whether to search")
    void libraryWithoutBasicLandsDoesNotForceSearch() {
        harness.addToBattlefield(player1, new SavageLands());
        harness.setLibrary(player1, List.of(new SavageLands(), new BalefulStrix()));

        castFromTheAshes();

        assertThat(gd.gameLog).extracting(GameLogEntry::plainText)
                .noneMatch(log -> log.contains("searches their library")
                        || log.endsWith("'s library is shuffled."));
        assertThat(gd.interaction.activeInteraction()).isNotNull();
    }

    @Test
    @DisplayName("No nonbasic lands means no searches or shuffles")
    void noNonbasicLandsDoesNotSearchOrShuffle() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Island()));

        castFromTheAshes();

        assertThat(activeSearch()).isNull();
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Island");
        assertThat(gd.gameLog).extracting(GameLogEntry::plainText)
                .noneMatch(log -> log.contains("searches their library")
                        || log.endsWith("'s library is shuffled."));
    }

    private PendingInteraction.LibrarySearch activeSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private void castFromTheAshes() {
        harness.setHand(player1, List.of(new FromTheAshes()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
