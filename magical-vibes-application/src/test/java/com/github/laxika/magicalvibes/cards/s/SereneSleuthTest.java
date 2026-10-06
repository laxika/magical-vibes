package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DisruptDecorum;
import com.github.laxika.magicalvibes.cards.k.KardurDoomscourge;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SereneSleuth.class, SolemnSimulacrum.class, DisruptDecorum.class, KardurDoomscourge.class})
class SereneSleuthTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a Clue token")
    void investigatesOnEnter() {
        harness.castFromHand(player1, new SereneSleuth(), "{1}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Investigates for each goaded creature, then removes goad")
    void investigatesAndRemovesGoadAtBeginningOfCombat() {
        Permanent sleuth = addCreatureReady(player2, new SereneSleuth());
        Permanent firstCreature = addCreatureReady(player2, new SolemnSimulacrum());
        Permanent secondCreature = addCreatureReady(player2, new SolemnSimulacrum());

        castDisruptDecorum();
        assertThat(gqs.isGoaded(gd, sleuth)).isTrue();
        assertThat(gqs.isGoaded(gd, firstCreature)).isTrue();
        assertThat(gqs.isGoaded(gd, secondCreature)).isTrue();

        beginCombat(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Clue")).hasSize(3);
        assertThat(gqs.isGoaded(gd, sleuth)).isFalse();
        assertThat(gqs.isGoaded(gd, firstCreature)).isFalse();
        assertThat(gqs.isGoaded(gd, secondCreature)).isFalse();
    }

    @Test
    @DisplayName("A second Sleuth does not investigate for creatures already freed from goad")
    void secondSleuthDoesNotCountRemovedGoad() {
        Permanent first = addCreatureReady(player2, new SereneSleuth());
        Permanent second = addCreatureReady(player2, new SereneSleuth());
        castDisruptDecorum();

        beginCombat(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Clue")).hasSize(2);
        assertThat(gqs.isGoaded(gd, first)).isFalse();
        assertThat(gqs.isGoaded(gd, second)).isFalse();
    }

    @Test
    @DisplayName("Kardur's attack requirements do not make creatures goaded")
    void doesNotInvestigateForKardurAttackRequirements() {
        addCreatureReady(player2, new SereneSleuth());
        addCreatureReady(player2, new SolemnSimulacrum());
        harness.castFromHand(player1, new KardurDoomscourge(), "{2}{B}{R}");
        harness.passBothPriorities();
        resolveAllTriggers();

        beginCombat(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("No goaded creatures means no combat Clues")
    void noGoadedCreaturesCreatesNoClues() {
        addCreatureReady(player1, new SereneSleuth());
        addCreatureReady(player1, new SolemnSimulacrum());

        beginCombat(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("The combat ability does not trigger on an opponent's turn")
    void doesNotTriggerOnOpponentsCombat() {
        Permanent sleuth = addCreatureReady(player2, new SereneSleuth());
        castDisruptDecorum();

        beginCombat(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Clue")).isEmpty();
        assertThat(gqs.isGoaded(gd, sleuth)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering after a snapshot goad are not counted")
    void doesNotCountCreaturesEnteringAfterGoad() {
        addCreatureReady(player2, new SereneSleuth());
        castDisruptDecorum();
        Permanent laterCreature = addCreatureReady(player2, new SolemnSimulacrum());

        beginCombat(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Clue")).hasSize(1);
        assertThat(gqs.isGoaded(gd, laterCreature)).isFalse();
    }

    private void castDisruptDecorum() {
        harness.castFromHand(player1, new DisruptDecorum(), "{2}{R}{R}");
        harness.passBothPriorities();
    }

    private void beginCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}