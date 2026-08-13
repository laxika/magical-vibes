package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WindfallTest extends BaseCardTest {

    @Test
    @DisplayName("Each player draws equal to the greatest hand discarded")
    void everyoneDrawsGreatestDiscarded() {
        setDeck(player1, List.of(new Island(), new Island(), new Island()));
        setDeck(player2, List.of(new Plains(), new Plains(), new Plains()));
        harness.setHand(player1, List.of(new Windfall(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new Plains(), new Plains(), new Plains()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).allMatch(card -> card instanceof Island);
        assertThat(gd.playerHands.get(player2.getId())).allMatch(card -> card instanceof Plains);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("With empty hands, Windfall discards and draws nothing")
    void emptyHandsDoNothing() {
        harness.setHand(player1, List.of(new Windfall()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
