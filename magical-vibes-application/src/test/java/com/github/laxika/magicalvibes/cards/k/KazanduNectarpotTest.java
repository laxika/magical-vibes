package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KazanduNectarpot.class, Forest.class})
class KazanduNectarpotTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gains life only when its trigger resolves")
    void gainsLifeOnlyWhenTriggerResolves() {
        harness.addToBattlefield(player1, new KazanduNectarpot());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Nectarpot triggers independently for the same land")
    void eachNectarpotTriggersIndependently() {
        harness.addToBattlefield(player1, new KazanduNectarpot());
        harness.addToBattlefield(player1, new KazanduNectarpot());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Gains 1 life when a land you control enters")
    void gainsLifeWhenControllerPlaysLand() {
        harness.addToBattlefield(player1, new KazanduNectarpot());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's land enters")
    void doesNotTriggerForOpponentsLand() {
        harness.addToBattlefield(player1, new KazanduNectarpot());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }
}
