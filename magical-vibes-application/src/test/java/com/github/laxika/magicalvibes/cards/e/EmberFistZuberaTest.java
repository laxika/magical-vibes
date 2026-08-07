package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmberFistZuberaTest extends BaseCardTest {

    // "When this creature dies, it deals damage to any target equal to the number of Zubera that died this turn."

    private void startMainPhase(int murders) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        List<Card> hand = new ArrayList<>();
        for (int i = 0; i < murders; i++) {
            hand.add(new Murder());
        }
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.BLACK, 6);
    }

    @Test
    @DisplayName("Dies alone: deals 1 damage to the chosen player")
    void diesAloneDealsOne() {
        Permanent zubera = harness.addToBattlefieldAndReturn(player1, new EmberFistZubera());
        harness.setLife(player2, 20);
        startMainPhase(1);

        harness.castInstant(player1, 0, zubera.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Counts every Zubera that died this turn, including itself")
    void countsAllZuberaDeathsThisTurn() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EmberFistZubera());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new EmberFistZubera());
        harness.setLife(player2, 20);
        startMainPhase(2);

        harness.castInstant(player1, 0, first.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        harness.castInstant(player1, 0, second.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Non-Zubera deaths do not increase the damage")
    void nonZuberaDeathsDoNotCount() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent zubera = harness.addToBattlefieldAndReturn(player1, new EmberFistZubera());
        harness.setLife(player2, 20);
        startMainPhase(2);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.castInstant(player1, 0, zubera.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Damage can be aimed at a creature and kills it when lethal")
    void damageCanKillCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EmberFistZubera());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new EmberFistZubera());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        startMainPhase(2);

        harness.castInstant(player1, 0, first.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(bearsId));

        harness.castInstant(player1, 0, second.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
