package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PillageTheBog.class, Forest.class, GrizzlyBears.class, Shock.class})
class PillageTheBogTest extends BaseCardTest {

    @Test
    @DisplayName("Looks at twice the lands controlled, keeps one, and randomly bottoms the rest")
    void looksAtTwiceLandsAndBottomsUnchosenCards() {
        Card[] top = {
                new GrizzlyBears(), new Shock(), new GrizzlyBears(),
                new Shock(), new GrizzlyBears(), new Shock()
        };
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new PillageTheBog()));
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(top[1].getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(top[1]);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).startsWith(top[4], top[5]);
        assertThat(deck.subList(2, deck.size())).containsExactlyInAnyOrder(top[0], top[2], top[3]);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
