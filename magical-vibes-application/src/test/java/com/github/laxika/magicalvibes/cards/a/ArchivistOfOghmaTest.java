package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DiabolicTutor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RampantGrowth;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchivistOfOghma.class, DiabolicTutor.class, GrizzlyBears.class, RampantGrowth.class})
class ArchivistOfOghmaTest extends BaseCardTest {

    @Test
    @DisplayName("When an opponent searches their library, you gain life and draw a card")
    void triggersWhenOpponentSearchesLibrary() {
        harness.addToBattlefield(player2, new ArchivistOfOghma());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new DiabolicTutor(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(21);
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .isInstanceOf(GrizzlyBears.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when you search your own library")
    void doesNotTriggerWhenControllerSearchesOwnLibrary() {
        harness.addToBattlefield(player1, new ArchivistOfOghma());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new DiabolicTutor(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement()
                .isInstanceOf(GrizzlyBears.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Searching an empty library still triggers Archivist")
    void triggersWhenOpponentSearchesEmptyLibrary() {
        harness.addToBattlefield(player2, new ArchivistOfOghma());
        harness.setLibrary(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new DiabolicTutor()));

        harness.castFromHand(player1, new DiabolicTutor(), "{2}{B}{B}");
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player2, 21);
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement().isInstanceOf(DiabolicTutor.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Searching for a basic land still triggers when the library has no basic lands")
    void triggersWhenOpponentFindsNoMatchingCards() {
        harness.addToBattlefield(player2, new ArchivistOfOghma());
        harness.setLibrary(player1, List.of(new DiabolicTutor()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new DiabolicTutor()));

        harness.castFromHand(player1, new RampantGrowth(), "{1}{G}");
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player2, 21);
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement().isInstanceOf(DiabolicTutor.class);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Archivist flashed in before a search spell resolves sees the search")
    void flashedInArchivistTriggersForPendingSearchSpell() {
        harness.setLibrary(player1, List.of(new DiabolicTutor()));
        harness.setLibrary(player2, List.of(new DiabolicTutor()));
        harness.castFromHand(player1, new DiabolicTutor(), "{2}{B}{B}");

        harness.castFromHand(player2, new ArchivistOfOghma(), "{1}{W}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Archivist of Oghma");
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 21);
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement().isInstanceOf(DiabolicTutor.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Archivist triggers independently for the same search")
    void multipleArchivistsEachGainLifeAndDraw() {
        harness.addToBattlefield(player2, new ArchivistOfOghma());
        harness.addToBattlefield(player2, new ArchivistOfOghma());
        harness.setLibrary(player1, List.of(new DiabolicTutor()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new DiabolicTutor(), new DiabolicTutor()));

        harness.castFromHand(player1, new DiabolicTutor(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player2, 22);
        assertThat(gd.playerHands.get(player2.getId()))
                .hasSize(2).allMatch(card -> card instanceof DiabolicTutor);
        assertThat(gd.stack).isEmpty();
    }
}
