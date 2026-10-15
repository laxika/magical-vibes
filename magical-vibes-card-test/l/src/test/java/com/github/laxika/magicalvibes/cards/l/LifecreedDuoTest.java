package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.h.HopToIt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LifecreedDuo.class, HopToIt.class})
class LifecreedDuoTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when another creature you control enters")
    void gainsLifeOnAllyCreatureEnter() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new LifecreedDuo());
        harness.setHand(player1, List.of(new LifecreedDuo()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Does not trigger when Lifecreed Duo enters")
    void noLifeOnSelfEnter() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new LifecreedDuo()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature enters")
    void noLifeOnOpponentCreatureEnter() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new LifecreedDuo());
        harness.setHand(player2, List.of(new LifecreedDuo()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each Rabbit token creates a separate life-gain trigger")
    void gainsLifeForEachTokenAfterItsTriggerResolves() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new LifecreedDuo());
        harness.setHand(player1, List.of(new HopToIt()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(3);
        harness.assertLife(player1, 20);
        for (int expectedLife = 21; expectedLife <= 23; expectedLife++) {
            harness.passBothPriorities();
            harness.assertLife(player1, expectedLife);
        }
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each Lifecreed Duo triggers for each entering creature")
    void multipleDuosEachTriggerForEveryToken() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new LifecreedDuo());
        harness.addToBattlefield(player1, new LifecreedDuo());
        harness.setHand(player1, List.of(new HopToIt()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(6);
        for (int i = 0; i < 6; i++) {
            harness.passBothPriorities();
        }
        harness.assertLife(player1, 26);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent's Rabbit tokens do not trigger life gain")
    void opponentTokensDoNotTriggerLifeGain() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new LifecreedDuo());
        harness.setHand(player2, List.of(new HopToIt()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, 0);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }
}
