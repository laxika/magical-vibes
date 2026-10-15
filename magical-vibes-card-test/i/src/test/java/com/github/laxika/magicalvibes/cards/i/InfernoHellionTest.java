package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfernoHellion.class, GreenwoodSentinel.class, Disperse.class})
class InfernoHellionTest extends BaseCardTest {

    private void advanceToEndStep(Player activePlayer) {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Does not trigger if it did not attack or block this turn")
    void doesNotTriggerWithoutAttackingOrBlocking() {
        addCreatureReady(player1, new InfernoHellion());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Inferno Hellion");
    }

    @Test
    @DisplayName("Shuffles itself into its owner's library after attacking")
    void shufflesAfterAttacking() {
        addCreatureReady(player1, new InfernoHellion());

        declareAttackers(player1, List.of(0));
        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);

        harness.assertNotOnBattlefield(player1, "Inferno Hellion");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Inferno Hellion"));
    }

    @Test
    @DisplayName("Shuffles itself into its owner's library after blocking")
    void shufflesAfterBlocking() {
        addCreatureReady(player1, new GreenwoodSentinel());
        addCreatureReady(player2, new InfernoHellion());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);

        harness.assertNotOnBattlefield(player2, "Inferno Hellion");
        assertThat(gameLogContains("Inferno Hellion is shuffled into its owner's library.")).isTrue();
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Inferno Hellion"));
    }

    @Test
    @DisplayName("A controlled Hellion shuffles into its owner's library")
    void shufflesIntoOwnersLibraryRatherThanControllers() {
        InfernoHellion card = new InfernoHellion();
        card.setOwnerId(player2.getId());
        addCreatureReady(player1, card);

        declareAttackers(player1, List.of(0));
        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);

        harness.assertNotOnBattlefield(player1, "Inferno Hellion");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(card);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A Hellion returned to hand in response is not shuffled away")
    void doesNotShuffleSourceThatLeftBattlefield() {
        InfernoHellion card = new InfernoHellion();
        Permanent hellion = addCreatureReady(player1, card);

        declareAttackers(player1, List.of(0));
        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, hellion.getId());
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);

        harness.assertNotOnBattlefield(player1, "Inferno Hellion");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Tramples over a blocker and still shuffles after combat ends")
    void tramplesAndShufflesAfterCompletedCombat() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        addCreatureReady(player1, new InfernoHellion());
        Permanent blocker = addCreatureReady(player2, new GreenwoodSentinel());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 5));

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        harness.assertOnBattlefield(player1, "Inferno Hellion");

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);

        harness.assertNotOnBattlefield(player1, "Inferno Hellion");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Inferno Hellion"));
    }
}
