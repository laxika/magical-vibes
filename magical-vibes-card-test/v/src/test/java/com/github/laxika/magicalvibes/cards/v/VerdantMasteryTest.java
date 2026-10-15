package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AmuletOfVigor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.OppositionAgent;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PrismariCampus;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VerdantMastery.class, Forest.class, Island.class, Mountain.class, Plains.class,
        PrismariCampus.class, OppositionAgent.class, AmuletOfVigor.class})
class VerdantMasteryTest extends BaseCardTest {

    @Test
    @DisplayName("Normal casting puts two chosen basics onto the controller's battlefield and the rest into hand")
    void normalCastDistributesChosenLands() {
        Card forest = new Forest();
        Card island = new Island();
        Card mountain = new Mountain();
        Card plains = new Plains();
        cast(List.of(forest, island, mountain, plains), false);

        harness.handleMultipleCardsChosen(player1,
                List.of(forest.getId(), island.getId(), mountain.getId(), plains.getId()));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.VerdantMasteryLandChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId(), island.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(forest.getId(), island.getId())
                .doesNotContain(mountain.getId(), plains.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(forest.getId())
                        || permanent.getCard().getId().equals(island.getId()))
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(mountain.getId(), plains.getId());
    }

    @Test
    @DisplayName("Alternate casting gives one chosen basic to an opponent and two to the controller")
    void alternateCastGivesOpponentOneLand() {
        Card forest = new Forest();
        Card island = new Island();
        Card mountain = new Mountain();
        Card plains = new Plains();
        cast(List.of(forest, island, mountain, plains), true);

        harness.handleMultipleCardsChosen(player1,
                List.of(forest.getId(), island.getId(), mountain.getId(), plains.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.VerdantMasteryLandChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(island.getId(), mountain.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(forest.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(island.getId(), mountain.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(plains.getId());
    }

    @Test
    @DisplayName("Declining the search finds no lands and still shuffles")
    void canFindNone() {
        Card forest = new Forest();
        cast(List.of(forest), false);

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).contains(forest);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard().getId().equals(forest.getId()));
    }

    @ParameterizedTest
    @CsvSource({"1, false", "2, false", "3, false", "1, true", "2, true", "3, true"})
    void distributesFewerThanFourLands(int count, boolean alternate) {
        List<Card> lands = IntStream.range(0, count).mapToObj(i -> (Card) new Forest()).toList();
        cast(lands, alternate);

        harness.handleMultipleCardsChosen(player1, lands.stream().map(Card::getId).toList());
        if (alternate) {
            harness.handleMultipleCardsChosen(player1, List.of(lands.getFirst().getId()));
        } else if (count > 2) {
            harness.handleMultipleCardsChosen(player1,
                    lands.subList(0, 2).stream().map(Card::getId).toList());
        }

        int ownStart = alternate ? 1 : 0;
        int ownEnd = Math.min(count, ownStart + 2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrderElementsOf(
                        lands.subList(ownStart, ownEnd).stream().map(Card::getId).toList());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyElementsOf(alternate ? List.of(lands.getFirst().getId()) : List.of());
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player2.getId())).allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(lands.subList(ownEnd, count));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void alternateCastCanFindNoneWithoutGivingOpponentALand() {
        Card forest = new Forest();
        cast(List.of(forest), true);

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void searchOffersOnlyBasicLands() {
        Card forest = new Forest();
        Card nonland = new VerdantMastery();
        Card nonbasic = new PrismariCampus();
        cast(List.of(forest, nonland, nonbasic), false);

        PendingInteraction.VerdantMasterySearchChoice search =
                gd.interaction.activeInteraction(PendingInteraction.VerdantMasterySearchChoice.class);
        assertThat(search.validCardIds()).containsExactly(forest.getId());
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(nonland, nonbasic);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId()).containsExactly(forest.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void opposingAgentControlsTheLibrarySearch() {
        harness.addToBattlefield(player2, new OppositionAgent());
        cast(List.of(new Forest()), false);

        assertThat(gd.interaction.activeInteraction().decidingPlayerId()).isEqualTo(player2.getId());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void enteringTappedTriggersRecipientsAmulet(boolean alternate) {
        harness.addToBattlefield(alternate ? player2 : player1, new AmuletOfVigor());
        Card forest = new Forest();
        cast(List.of(forest), alternate);

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        if (alternate) {
            harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        }
        resolveAllTriggers();

        assertThat(findPermanent(alternate ? player2 : player1, "Forest").isTapped()).isFalse();
    }

    @ParameterizedTest
    @CsvSource({"false, false", "false, true", "true, false", "true, true"})
    void resolvesWithNoBasicLands(boolean nonemptyLibrary, boolean alternate) {
        List<Card> library = nonemptyLibrary ? List.of(new VerdantMastery()) : List.of();
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new VerdantMastery()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, alternate ? 3 : 5);
        if (alternate) {
            harness.castWithAlternateCost(player1, 0, List.of());
            harness.passBothPriorities();
        } else {
            harness.castAndResolveSorcery(player1, 0, 0);
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Verdant Mastery");
    }

    private void cast(List<Card> library, boolean alternate) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new VerdantMastery()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, alternate ? 3 : 5);
        if (alternate) {
            harness.castWithAlternateCost(player1, 0, List.of());
            harness.passBothPriorities();
        } else {
            harness.castAndResolveSorcery(player1, 0, 0);
        }
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.VerdantMasterySearchChoice.class);
    }
}
