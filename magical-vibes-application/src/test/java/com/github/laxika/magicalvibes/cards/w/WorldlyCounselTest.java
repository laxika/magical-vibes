package com.github.laxika.magicalvibes.cards.w;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WorldlyCounsel.class, Plains.class, Island.class, Swamp.class})
class WorldlyCounselTest extends BaseCardTest {

    private void castWorldlyCounsel(Card... top) {
        harness.setLibrary(player1, List.of(top));
        harness.castFromHand(player1, new WorldlyCounsel(), "{1}{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Domain 2: looks at top two, one to hand, the other on the bottom")
    void domainTwoKeepsOne() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        Card top1 = new Plains();
        Card top2 = new Island();

        castWorldlyCounsel(top1, top2);
        harness.handleMultipleCardsChosen(player1, List.of(top1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(top1).doesNotContain(top2);
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(top2);
    }

    @Test
    @DisplayName("Domain 3: looks at top three, one to hand, the rest ordered on the bottom")
    void domainThreeReordersRest() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        Card top1 = new Plains();
        Card top2 = new Island();
        Card top3 = new Swamp();

        castWorldlyCounsel(top1, top2, top3);
        harness.handleMultipleCardsChosen(player1, List.of(top2.getId()));
        // The two unchosen cards are ordered onto the bottom of the library.
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).contains(top2).doesNotContain(top1, top3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2)
                .containsExactly(top1, top3);
    }

    @Test
    @DisplayName("Duplicate basic land types count only once toward Domain")
    void duplicateTypesCountOnce() {
        // Two Plains + one Island = 2 basic land types, so Worldly Counsel looks at the top two only.
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        Card top1 = new Plains();
        Card top2 = new Island();
        Card top3 = new Swamp();

        castWorldlyCounsel(top1, top2, top3);
        // Only top1 and top2 were looked at; top3 stays untouched at the bottom of the library.
        harness.handleMultipleCardsChosen(player1, List.of(top1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(top1).doesNotContain(top2, top3);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top3, top2);
    }

    @Test
    @DisplayName("Domain 0 leaves the library unchanged")
    void noBasicLandTypesDoesNothing() {
        Card top = new Plains();

        castWorldlyCounsel(top);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library shorter than Domain puts every available card into hand")
    void shortLibraryPutsAvailableCardsIntoHand() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        Card onlyCard = new Island();

        castWorldlyCounsel(onlyCard);

        assertThat(gd.playerHands.get(player1.getId())).contains(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Domain is counted when the spell resolves")
    void domainUsesLandsControlledAtResolution() {
        harness.addToBattlefield(player1, new Plains());
        Card top1 = new Plains();
        Card top2 = new Island();
        Card top3 = new Swamp();
        harness.setLibrary(player1, List.of(top1, top2, top3));
        harness.castFromHand(player1, new WorldlyCounsel(), "{1}{U}");

        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(top1.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).contains(top1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top2, top3);
    }
}
