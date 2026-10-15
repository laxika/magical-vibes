package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BoseijuWhoEndures;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KuraTheBoundlessSky.class, Forest.class, Island.class, Mountain.class, BoseijuWhoEndures.class})
class KuraTheBoundlessSkyTest extends BaseCardTest {

    private static final String SEARCH_MODE =
            "Search your library for up to three land cards, reveal them, and put them into your hand";
    private static final String TOKEN_MODE =
            "Create an X/X green Spirit creature token, where X is the number of lands you control";

    @Test
    @DisplayName("The search mode puts up to three land cards from the library into your hand")
    void searchModeFindsUpToThreeLands() {
        Forest forest = new Forest();
        Island island = new Island();
        Mountain mountain = new Mountain();
        KuraTheBoundlessSky nonland = new KuraTheBoundlessSky();
        harness.setLibrary(player1, List.of(forest, island, mountain, nonland));
        harness.addToBattlefield(player1, new KuraTheBoundlessSky());

        killKura();
        harness.handleListChoice(player1, SEARCH_MODE);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactlyInAnyOrder(forest, island, mountain);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest, island, mountain);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
    }

    @Test
    @DisplayName("The token mode creates a Spirit whose power and toughness equal your land count")
    void tokenModeUsesControlledLandCount() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new KuraTheBoundlessSky());

        killKura();
        harness.handleListChoice(player1, TOKEN_MODE);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Spirit"))
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().getPower()).isEqualTo(2);
                    assertThat(token.getCard().getToughness()).isEqualTo(2);
                });
    }

    @Test
    @DisplayName("The search finds at most three cards and permits duplicate land names")
    void searchModeStopsAtThreeLandsWithTheSameName() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        Forest fourth = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.addToBattlefield(player1, new KuraTheBoundlessSky());

        killKura();
        harness.handleListChoice(player1, SEARCH_MODE);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(first, second, third).doesNotContain(fourth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The search mode can find a nonbasic land and stop after one card")
    void searchModeCanFindNonbasicLandAndStopEarly() {
        BoseijuWhoEndures boseiju = new BoseijuWhoEndures();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(boseiju, forest));
        harness.addToBattlefield(player1, new KuraTheBoundlessSky());

        killKura();
        harness.handleListChoice(player1, SEARCH_MODE);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(boseiju).doesNotContain(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("The search mode may find zero cards even when lands are available")
    void searchModeCanFindZeroLands() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addToBattlefield(player1, new KuraTheBoundlessSky());

        killKura();
        harness.handleListChoice(player1, SEARCH_MODE);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The search mode completes when there are no land cards to find")
    void searchModeWithNoLands() {
        KuraTheBoundlessSky nonland = new KuraTheBoundlessSky();
        harness.setLibrary(player1, List.of(nonland));
        harness.addToBattlefield(player1, new KuraTheBoundlessSky());

        killKura();
        harness.handleListChoice(player1, SEARCH_MODE);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(nonland);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The Spirit uses the land count at resolution and keeps that size afterward")
    void tokenSizeIsFixedAtResolutionAndIgnoresOpponentsLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player1, new KuraTheBoundlessSky());

        killKura();
        harness.handleListChoice(player1, TOKEN_MODE);
        harness.addToBattlefield(player1, new BoseijuWhoEndures());
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);

        harness.addToBattlefield(player1, new Island());
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("With no lands the zero-toughness Spirit dies")
    void tokenModeWithNoLands() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new KuraTheBoundlessSky());

        killKura();
        harness.handleListChoice(player1, TOKEN_MODE);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private void killKura() {
        Permanent kura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof KuraTheBoundlessSky)
                .findFirst()
                .orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, kura));
        harness.passBothPriorities();
    }
}
