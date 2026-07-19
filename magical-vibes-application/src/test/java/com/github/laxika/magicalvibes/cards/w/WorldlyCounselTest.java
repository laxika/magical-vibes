package com.github.laxika.magicalvibes.cards.w;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WorldlyCounselTest extends BaseCardTest {

    private List<Card> setupDeck(Card... top) {
        List<Card> cards = List.of(top);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards); // index 0 is the top of the library
        harness.setHand(player1, List.of(new WorldlyCounsel()));
        harness.addMana(player1, ManaColor.BLUE, 2); // {1}{U}
        return cards;
    }

    @Test
    @DisplayName("Domain 2: looks at top two, one to hand, the other on the bottom")
    void domainTwoKeepsOne() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        Card top1 = new GrizzlyBears();
        Card top2 = new LlanowarElves();
        setupDeck(top1, top2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
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
        Card top1 = new GrizzlyBears();
        Card top2 = new LlanowarElves();
        Card top3 = new GrizzlyBears();
        setupDeck(top1, top2, top3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(top2.getId()));
        // The two unchosen cards are ordered onto the bottom of the library.
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).contains(top2).doesNotContain(top1, top3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2)
                .containsExactlyInAnyOrder(top1, top3);
    }

    @Test
    @DisplayName("Duplicate basic land types count only once toward Domain")
    void duplicateTypesCountOnce() {
        // Two Plains + one Island = 2 basic land types, so Worldly Counsel looks at the top two only.
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        Card top1 = new GrizzlyBears();
        Card top2 = new LlanowarElves();
        Card top3 = new GrizzlyBears();
        setupDeck(top1, top2, top3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        // Only top1 and top2 were looked at; top3 stays untouched at the bottom of the library.
        harness.handleMultipleCardsChosen(player1, List.of(top1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(top1).doesNotContain(top2, top3);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top3, top2);
    }
}
