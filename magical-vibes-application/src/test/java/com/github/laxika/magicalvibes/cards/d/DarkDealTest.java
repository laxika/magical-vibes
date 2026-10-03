package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.NotionThief;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarkDeal.class, GrizzlyBears.class, HillGiant.class, Island.class,
        Plains.class, Forest.class, Mountain.class, NotionThief.class})
class DarkDealTest extends BaseCardTest {

    @Test
    @DisplayName("Each player discards their hand and draws one fewer card")
    void eachPlayerDiscardsAndDrawsOneFewer() {
        harness.setHand(player1, List.of(new DarkDeal(), new GrizzlyBears(), new HillGiant(), new Island()));
        harness.setHand(player2, List.of(new Plains(), new Forest()));
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain()));
        harness.setLibrary(player2, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2)
                .allMatch(card -> card.getName().equals("Mountain"));
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1)
                .allMatch(card -> card.getName().equals("Island"));
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Dark Deal");
    }

    @Test
    @DisplayName("A one-card hand draws zero cards")
    void oneCardHandDrawsZero() {
        harness.setHand(player1, List.of(new DarkDeal()));
        harness.setHand(player2, List.of(new Plains()));
        harness.setLibrary(player2, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An empty opposing hand does not prevent the caster from drawing")
    void emptyOpponentHandDoesNotDraw() {
        harness.setHand(player1, List.of(new DarkDeal(), new Plains(), new Forest()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setLibrary(player2, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInHand(player1, "Mountain");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("All hands are discarded before Notion Thief redirects any draws")
    void redirectedDrawsAreNotDiscardedByTheOtherPlayer() {
        harness.addToBattlefield(player2, new NotionThief());
        harness.setHand(player1, List.of(new DarkDeal(), new Plains(), new Plains(), new Plains()));
        harness.setHand(player2, List.of(new Plains(), new Plains()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setLibrary(player2, List.of(new Island(), new Island(), new Forest(),
                new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Island", "Island", "Forest");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2)
                .allMatch(card -> card.getName().equals("Plains"));
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
