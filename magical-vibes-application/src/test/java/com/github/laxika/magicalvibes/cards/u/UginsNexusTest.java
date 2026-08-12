package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.CaptureOfJingzhou;
import com.github.laxika.magicalvibes.cards.k.KuldothaRebirth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class UginsNexusTest extends BaseCardTest {

    private void enableAutoStop() {
        Set<TurnStep> stops1 = ConcurrentHashMap.newKeySet();
        stops1.add(TurnStep.PRECOMBAT_MAIN);
        gd.playerAutoStopSteps.put(player1.getId(), stops1);
        Set<TurnStep> stops2 = ConcurrentHashMap.newKeySet();
        stops2.add(TurnStep.PRECOMBAT_MAIN);
        gd.playerAutoStopSteps.put(player2.getId(), stops2);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("An extra turn is skipped while Ugin's Nexus remains on the battlefield")
    void skipsExtraTurnWhileOnBattlefield() {
        enableAutoStop();
        harness.addToBattlefield(player1, new UginsNexus());

        harness.setHand(player1, List.of(new CaptureOfJingzhou()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        int turnBefore = gd.turnNumber;
        advanceTurn();

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing Ugin's Nexus exiles it and queues an extra turn")
    void sacrificingNexusExilesItAndQueuesExtraTurn() {
        enableAutoStop();
        Permanent nexus = new Permanent(new UginsNexus());
        gd.playerBattlefields.get(player1.getId()).add(nexus);

        harness.setHand(player1, List.of(new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorceryWithSacrifice(player1, 0, nexus.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(nexus);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(nexus.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(nexus.getCard().getId()));
        assertThat(gd.extraTurns).containsExactly(player1.getId());

        advanceTurn();

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.extraTurns).isEmpty();
    }
}
