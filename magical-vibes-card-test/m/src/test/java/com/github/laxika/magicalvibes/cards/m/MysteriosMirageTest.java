package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.cards.p.PulsatingIllusion;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysteriosMirage.class, PulsatingIllusion.class, AvenFlock.class})
class MysteriosMirageTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 3/3 Illusion Villain at your end step after you discard")
    void createsTokenAfterControllerDiscards() {
        harness.addToBattlefield(player1, new MysteriosMirage());
        harness.addToBattlefield(player1, new PulsatingIllusion());
        harness.setHand(player1, List.of(new AvenFlock()));

        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Illusion Villain");
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().isToken()).isTrue();
        assertThat(tokens.getFirst().getEffectivePower()).isEqualTo(3);
        assertThat(tokens.getFirst().getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not create a token when you did not discard this turn")
    void doesNotCreateTokenWithoutDiscard() {
        harness.addToBattlefield(player1, new MysteriosMirage());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Illusion Villain")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new MysteriosMirage());
        harness.addToBattlefield(player1, new PulsatingIllusion());
        harness.setHand(player1, List.of(new AvenFlock()));

        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Illusion Villain")).isEmpty();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Multiple discards still create only one token")
    void multipleDiscardsCreateOneToken() {
        harness.addToBattlefield(player1, new MysteriosMirage());
        harness.addToBattlefield(player1, new PulsatingIllusion());
        harness.addToBattlefield(player1, new PulsatingIllusion());
        harness.setHand(player1, List.of(new AvenFlock(), new AvenFlock()));

        for (int permanentIndex = 1; permanentIndex <= 2; permanentIndex++) {
            harness.activateAbility(player1, permanentIndex, null, null);
            harness.handleCardChosen(player1, 0);
            harness.passBothPriorities();
        }

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Illusion Villain")).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's discard does not enable your end-step trigger")
    void opponentsDiscardDoesNotEnableTrigger() {
        harness.addToBattlefield(player1, new MysteriosMirage());
        harness.addToBattlefield(player2, new PulsatingIllusion());
        harness.setHand(player2, List.of(new AvenFlock()));
        harness.ensurePriority(player2);
        harness.activateAbility(player2, 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Illusion Villain")).isEmpty();
    }

    @Test
    @DisplayName("A discard before Mirage enters still enables its end-step trigger")
    void discardBeforeMirageEntersCounts() {
        harness.addToBattlefield(player1, new PulsatingIllusion());
        harness.setHand(player1, List.of(new AvenFlock()));
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new MysteriosMirage());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Illusion Villain")).hasSize(1);
    }

    @Test
    @DisplayName("Discarding after the end step begins does not trigger Mirage")
    void discardDuringEndStepIsTooLate() {
        harness.addToBattlefield(player1, new MysteriosMirage());
        harness.addToBattlefield(player1, new PulsatingIllusion());
        harness.setHand(player1, List.of(new AvenFlock()));
        advanceToEndStep(player1);
        assertThat(gd.stack).isEmpty();

        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Illusion Villain")).isEmpty();
    }
}
