package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Skinrender;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ObeliskSpiderTest extends BaseCardTest {

    private void resolveStack() {
        for (int guard = 0; guard < 40 && !gd.stack.isEmpty() && !gd.interaction.isAwaitingInput(); guard++) {
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("Combat damage to a creature puts a -1/-1 counter and drains 1/gains 1")
    void combatDamagePutsCounterAndDrains() {
        Permanent spider = addCreatureReady(player1, new ObeliskSpider());
        spider.setAttacking(true);
        // 3/3: survives 1 combat damage + one -1/-1; deals only 3 so the 1/4 spider also lives.
        addCreatureReady(player2, new HillGiant());

        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveStack();

        Permanent giant = findPermanent(player2, "Hill Giant");
        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before - 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before + 1);
    }

    @Test
    @DisplayName("No drain when dealing combat damage to a player (no creature damaged)")
    void noTriggerOnDamageToPlayer() {
        Permanent spider = addCreatureReady(player1, new ObeliskSpider());
        spider.setAttacking(true);

        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveStack();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before - 1); // combat damage only
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before);
    }

    @Test
    @DisplayName("Putting multiple -1/-1 counters at once drains only once")
    void multipleCountersAtOnceDrainOnce() {
        harness.addToBattlefield(player1, new ObeliskSpider());
        harness.addToBattlefield(player2, new AirElemental());
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        harness.getGameService().playCard(gd, player1, 0, 0, targetId, null);
        resolveStack();

        Permanent airElemental = findPermanent(player2, "Air Elemental");
        assertThat(airElemental.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before - 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before + 1);
    }

    @Test
    @DisplayName("An opponent putting the -1/-1 counters does not trigger your Obelisk Spider")
    void opponentPlacingCountersDoesNotTrigger() {
        harness.addToBattlefield(player1, new ObeliskSpider());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player2, List.of(new Skinrender()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        int p1Before = gd.playerLifeTotals.get(player1.getId());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        harness.forceActivePlayer(player2);
        harness.getGameService().playCard(gd, player2, 0, 0, targetId, null);
        resolveStack();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before);
    }
}
