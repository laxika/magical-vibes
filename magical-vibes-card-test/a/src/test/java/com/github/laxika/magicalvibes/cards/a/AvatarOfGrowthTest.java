package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.ObNixilisUnshackled;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvatarOfGrowth.class, Forest.class, Plains.class, ObNixilisUnshackled.class})
class AvatarOfGrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less per opponent and lets each player fetch two basic lands")
    void reducesCostAndFetchesBasicLandsForEachPlayer() {
        harness.setHand(player1, List.of(new AvatarOfGrowth()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new AvatarOfGrowth()));
        harness.setLibrary(player2, List.of(new Plains(), new Plains(), new AvatarOfGrowth()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = activeSearch();
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Forest", "Forest");

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        search = activeSearch();
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Plains", "Plains");

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(activeSearch()).isNull();
        assertThat(landCount(player1)).isEqualTo(2);
        assertThat(landCount(player2)).isEqualTo(2);
    }

    @Test
    @DisplayName("Players may find zero or fewer than two lands")
    void playersMayFindFewerThanTwoLands() {
        harness.setHand(player1, List.of(new AvatarOfGrowth()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Plains(), new Plains()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player2, -1);

        assertThat(activeSearch()).isNull();
        assertThat(landCount(player1)).isEqualTo(1);
        assertThat(landCount(player2)).isZero();
    }

    @Test
    @DisplayName("A player with only one basic land still gets it untapped and the next player searches")
    void singleBasicLandDoesNotStopOtherPlayersSearch() {
        harness.setHand(player1, List.of(new AvatarOfGrowth()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of(new Forest(), new AvatarOfGrowth()));
        harness.setLibrary(player2, List.of(new Plains()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(activeSearch()).isNull();
        assertThat(landCount(player1)).isEqualTo(1);
        assertThat(landCount(player2)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .allMatch(permanent -> !permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .allMatch(permanent -> !permanent.isTapped());
    }

    @Test
    @DisplayName("An empty library does not prevent the other player from searching")
    void emptyLibraryDoesNotStopOtherPlayersSearch() {
        harness.setHand(player1, List.of(new AvatarOfGrowth()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(new Plains()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);
        assertThat(activeSearch()).isNull();
        assertThat(landCount(player1)).isZero();
        assertThat(landCount(player2)).isEqualTo(1);
    }

    @Test
    @CardUsed(ObNixilisUnshackled.class)
    @DisplayName("Finding zero lands still counts as searching the library")
    void findingZeroLandsStillTriggersSearchPenalty() {
        harness.addToBattlefield(player1, new ObNixilisUnshackled());
        harness.setHand(player1, List.of(new AvatarOfGrowth()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Plains(), new Plains()));
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player2, -1);

        assertThat(activeSearch()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 10);
        assertThat(landCount(player2)).isZero();
    }

    private PendingInteraction.LibrarySearch activeSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private long landCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.LAND))
                .count();
    }
}
