package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DarknessDescendsTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two -1/-1 counters on each creature controlled by either player")
    void putsTwoMinusOneMinusOneCountersOnEachCreature() {
        Permanent creature = addCreature(player1, largeCreature());
        Permanent opponentCreature = addCreature(player2, largeCreature());

        harness.setHand(player1, List.of(new DarknessDescends()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two counters cause a 2/2 creature to die")
    void twoCountersKillTwoTwoCreature() {
        addCreature(player1, new GrizzlyBears());
        addCreature(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new DarknessDescends()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    private Permanent addCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private static Card largeCreature() {
        Card card = new Card();
        card.setName("Large Creature");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setPower(4);
        card.setToughness(5);
        return card;
    }
}
