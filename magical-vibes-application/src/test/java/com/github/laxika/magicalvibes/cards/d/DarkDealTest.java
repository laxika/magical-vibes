package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DarkDealTest extends BaseCardTest {

    @Test
    @DisplayName("Each player discards their hand and draws one fewer card")
    void eachPlayerDiscardsAndDrawsOneFewer() {
        harness.setHand(player1, List.of(new DarkDeal(), new GrizzlyBears(), new HillGiant(), new Island()));
        harness.setHand(player2, List.of(new Plains(), new Forest()));
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain()));
        harness.setLibrary(player2, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

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

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }
}
