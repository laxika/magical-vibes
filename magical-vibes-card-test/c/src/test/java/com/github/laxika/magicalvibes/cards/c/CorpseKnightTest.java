package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorpseKnight.class, GreenwoodSentinel.class, RaiseTheAlarm.class})
class CorpseKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses 1 life when another creature you control enters")
    void drainsOpponentsOnAllyCreatureEnter() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new CorpseKnight());

        harness.castFromHand(player1, new GreenwoodSentinel(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature enters")
    void noTriggerOnOpponentCreatureEnter() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new CorpseKnight());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new GreenwoodSentinel(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not trigger when Corpse Knight itself enters")
    void noTriggerOnSelfEnter() {
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new CorpseKnight(), "{W}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Triggers separately for both tokens entering simultaneously")
    void triggersForEachTokenEntering() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new CorpseKnight());

        harness.castFromHand(player1, new RaiseTheAlarm(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each Corpse Knight triggers for another creature entering")
    void multipleKnightsTriggerIndependently() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new CorpseKnight());
        harness.addToBattlefield(player1, new CorpseKnight());

        harness.castFromHand(player1, new GreenwoodSentinel(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An existing Corpse Knight triggers when a second Corpse Knight enters")
    void anotherKnightEnteringTriggersOnlyExistingKnight() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new CorpseKnight());

        harness.castFromHand(player1, new CorpseKnight(), "{W}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }
}
