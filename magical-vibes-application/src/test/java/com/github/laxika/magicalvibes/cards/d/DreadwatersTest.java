package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DreadwatersTest extends BaseCardTest {

    @Test
    @DisplayName("Target player mills one card per land the caster controls")
    void millsForEachLandYouControl() {
        harness.setHand(player1, List.of(new Dreadwaters()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Mills nothing when the caster controls no lands")
    void millsNothingWithoutLands() {
        harness.setHand(player1, List.of(new Dreadwaters()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(10);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target its controller")
    void canTargetSelf() {
        harness.setHand(player1, List.of(new Dreadwaters()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        List<Card> deck = gd.playerDecks.get(player1.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        // Two milled cards plus Dreadwaters itself.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(8);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }
}
