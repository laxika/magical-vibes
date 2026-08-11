package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BurningCuriosityTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private List<Card> putTopCards(int count) {
        List<Card> cards = java.util.stream.IntStream.range(0, count)
                .<Card>mapToObj(i -> new RagingGoblin())
                .toList();
        gd.playerDecks.get(player1.getId()).addAll(0, cards);
        return cards;
    }

    @Test
    void withoutBlightExilesTopTwoCards() {
        List<Card> topCards = putTopCards(3);
        harness.setHand(player1, List.of(new BurningCuriosity()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(topCards.get(0).getId(), topCards.get(1).getId());
        assertThat(gd.playerDecks.get(player1.getId())).contains(topCards.get(2));
        assertThat(gd.exilePlayPermissions)
                .containsKeys(topCards.get(0).getId(), topCards.get(1).getId())
                .doesNotContainKey(topCards.get(2).getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd.get(topCards.get(0).getId()))
                .isEqualTo(gd.turnNumber + 2);
    }

    @Test
    void blightExilesTopThreeCards() {
        List<Card> topCards = putTopCards(4);
        Permanent blightCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BurningCuriosity()));
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, blightCreature.getId());
        harness.passBothPriorities();

        assertThat(blightCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(topCards.get(0).getId(), topCards.get(1).getId(), topCards.get(2).getId());
        assertThat(gd.playerDecks.get(player1.getId())).contains(topCards.get(3));
    }
}
