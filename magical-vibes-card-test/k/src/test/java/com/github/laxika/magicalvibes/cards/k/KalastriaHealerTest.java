package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CompleteDisregard;
import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.s.SnappingGnarlid;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KalastriaHealer.class, SnappingGnarlid.class, CompleteDisregard.class, Conspiracy.class})
class KalastriaHealerTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry makes each opponent lose 1 life and you gain 1 life")
    void ownEntryDrainsOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new KalastriaHealer(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Another Ally entering triggers each Kalastria Healer")
    void anotherAllyEntryTriggersEachHealer() {
        harness.addToBattlefield(player1, new KalastriaHealer());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new KalastriaHealer(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A non-Ally creature entering does not trigger it")
    void nonAllyEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new KalastriaHealer());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new SnappingGnarlid(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent's Ally entry does not trigger your Healer")
    void opponentAllyEntryDoesNotTriggerYourHealer() {
        harness.addToBattlefield(player1, new KalastriaHealer());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player2, new KalastriaHealer());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 21);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting an Ally does not drain life before it enters and the trigger resolves")
    void drainWaitsForEntryAndTriggerResolution() {
        harness.addToBattlefield(player1, new KalastriaHealer());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new KalastriaHealer(), "{1}{B}");
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The rally trigger still resolves after its source is exiled")
    void triggerResolvesAfterSourceLeaves() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        var healer = harness.enterBattlefieldAndReturn(player1, new KalastriaHealer());
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new CompleteDisregard()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, healer.getId());
        harness.assertNotOnBattlefield(player1, "Kalastria Healer");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Its own entry still triggers when Conspiracy replaces its Ally type")
    void ownEntryTriggersWithoutAllyType() {
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new KalastriaHealer(), "{1}{B}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Kalastria Healer");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }
}
