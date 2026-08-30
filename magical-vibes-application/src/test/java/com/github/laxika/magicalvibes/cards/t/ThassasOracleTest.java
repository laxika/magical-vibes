package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThassasOracle.class, GrizzlyBears.class})
class ThassasOracleTest extends BaseCardTest {

    @Test
    void looksAtBlueDevotionCardsAndPutsUpToOneOnTop() {
        Card chosen = new GrizzlyBears();
        Card otherLookedCard = new GrizzlyBears();
        Card untouched = new GrizzlyBears();
        harness.setLibrary(player1, List.of(chosen, otherLookedCard, untouched));

        castOracle();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(chosen, otherLookedCard);
        assertThat(search.params().canFailToFind()).isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3).contains(chosen, otherLookedCard, untouched);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(chosen);
    }

    @Test
    void mayPutNeitherLookedCardOnTop() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card untouched = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, untouched));

        castOracle();
        harness.handleCardChosen(player1, -1);

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(library).hasSize(3).contains(first, second, untouched);
        assertThat(library.getFirst()).isSameAs(untouched);
    }

    @Test
    void winsWhenDevotionEqualsLibrarySize() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));

        castOracle();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    void zeroDevotionDoesNotLookAtCardsWhenOracleLeavesBeforeResolution() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        ThassasOracle oracle = new ThassasOracle();
        harness.setHand(player1, List.of(oracle));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent oraclePermanent = findPermanent(player1, "Thassa's Oracle");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, oraclePermanent));
        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
    }

    private void castOracle() {
        harness.setHand(player1, List.of(new ThassasOracle()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
