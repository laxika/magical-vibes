package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class TemporalTrespassTest extends BaseCardTest {

    private void enableAutoStop() {
        Set<TurnStep> stops1 = ConcurrentHashMap.newKeySet();
        stops1.add(TurnStep.PRECOMBAT_MAIN);
        gd.playerAutoStopSteps.put(player1.getId(), stops1);
        Set<TurnStep> stops2 = ConcurrentHashMap.newKeySet();
        stops2.add(TurnStep.PRECOMBAT_MAIN);
        gd.playerAutoStopSteps.put(player2.getId(), stops2);
    }

    private void castTemporalTrespass() {
        harness.setHand(player1, List.of(new TemporalTrespass()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Resolving queues an extra turn and exiles Temporal Trespass")
    void resolvingQueuesExtraTurnAndExiles() {
        castTemporalTrespass();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Temporal Trespass"));
        harness.assertNotInGraveyard(player1, "Temporal Trespass");
    }

    @Test
    @DisplayName("The extra turn is taken by the caster before normal turn order resumes")
    void extraTurnIsTakenBeforeNormalTurnOrderResumes() {
        enableAutoStop();
        int turnBefore = gd.turnNumber;

        castTemporalTrespass();

        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);

        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 2);
    }

    @Test
    @DisplayName("Delve exiles graveyard cards to pay the generic mana")
    void delveExilesGraveyardCards() {
        List<Card> graveyard = List.of(
                new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock(), new Shock());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new TemporalTrespass()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, List.of(0, 1, 2, 3, 4, 5, 6, 7));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(graveyard);
    }
}
