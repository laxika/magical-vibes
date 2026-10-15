package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerfolkWayfinder.class, Forest.class, Island.class})
class MerfolkWayfinderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts all Island cards among the top three into hand")
    void putsIslandsIntoHand() {
        Island island1 = new Island();
        Forest forest = new Forest();
        Island island2 = new Island();
        Forest belowTopThree = new Forest();
        setupLibrary(island1, forest, island2, belowTopThree);

        castAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).contains(island1, island2);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest, belowTopThree);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(belowTopThree, forest);
    }

    @Test
    @DisplayName("ETB only looks at the top three cards")
    void onlyLooksAtTopThree() {
        Island island1 = new Island();
        Island island2 = new Island();
        Island island3 = new Island();
        Island belowTopThree = new Island();
        setupLibrary(island1, island2, island3, belowTopThree);

        castAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(island1, island2, island3);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(belowTopThree);
    }

    @Test
    @DisplayName("ETB does nothing when the library is empty")
    void emptyLibrary() {
        gd.playerDecks.get(player1.getId()).clear();

        castAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void shortLibraryStillMovesEveryIslandToHand() {
        Island island = new Island();
        Forest forest = new Forest();
        setupLibrary(island, forest);

        castAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void remainingCardsCanBeOrderedAfterMovingIslandToHand() {
        Forest first = new Forest();
        Island island = new Island();
        Forest second = new Forest();
        Island untouched = new Island();
        setupLibrary(first, island, second, untouched);

        castAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, second, first);
    }

    @Test
    void noIslandsStillAllowsOrderingEveryRevealedCardOnBottom() {
        Forest first = new Forest();
        Forest second = new Forest();
        MerfolkWayfinder third = new MerfolkWayfinder();
        Island untouched = new Island();
        setupLibrary(first, second, third, untouched);

        castAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, third, first, second);
    }

    private void setupLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new MerfolkWayfinder()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
