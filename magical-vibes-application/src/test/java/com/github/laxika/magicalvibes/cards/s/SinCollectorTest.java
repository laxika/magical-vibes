package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SinCollectorTest extends BaseCardTest {

    /** Casts Sin Collector targeting player2 and resolves both the spell and its ETB trigger. */
    private void castAndResolveETB() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SinCollector()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Only instant and sorcery cards are choosable")
    void onlyInstantsAndSorceriesChoosable() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Forest(), new Divination())));

        castAndResolveETB();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(1, 3);
    }

    @Test
    @DisplayName("Chosen instant is exiled from the opponent's hand")
    void chosenInstantIsExiled() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));

        castAndResolveETB();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).anyMatch(c -> c.getName().equals("Peek"));
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Hand with no instants or sorceries yields no choice")
    void noInstantsOrSorceriesNoChoice() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));

        castAndResolveETB();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no valid choices"));
    }

    @Test
    @DisplayName("Exiled card stays exiled when Sin Collector dies")
    void exiledCardStaysExiledWhenSinCollectorDies() {
        harness.setHand(player2, new ArrayList<>(List.of(new Peek())));

        castAndResolveETB();
        harness.handleCardChosen(player1, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID collectorId = harness.getPermanentId(player1, "Sin Collector");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, collectorId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sin Collector");
        assertThat(gd.getPlayerExiledCards(player2.getId())).anyMatch(c -> c.getName().equals("Peek"));
        assertThat(gd.playerHands.get(player2.getId())).noneMatch(c -> c.getName().equals("Peek"));
    }
}
