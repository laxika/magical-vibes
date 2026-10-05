package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.l.LoxodonMystic;
import com.github.laxika.magicalvibes.cards.e.EchoingTruth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NimAbomination.class, LoxodonMystic.class, EchoingTruth.class})
class NimAbominationTest extends BaseCardTest {

    private void reachEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    private void reachEndStep() {
        reachEndStep(player1);
    }

    private void resolveEndStepTrigger() {
        reachEndStep();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Controller loses 3 life when Nim Abomination is untapped at their end step")
    void losesLifeWhenUntappedAtEndStep() {
        harness.addToBattlefield(player1, new NimAbomination());
        harness.setLife(player1, 20);

        resolveEndStepTrigger();

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Does not trigger when Nim Abomination is tapped at the beginning of the end step")
    void doesNotTriggerWhenTappedAtEndStep() {
        Permanent nimAbomination = harness.addToBattlefieldAndReturn(player1, new NimAbomination());
        nimAbomination.tap();
        harness.setLife(player1, 20);

        resolveEndStepTrigger();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new NimAbomination());
        harness.setLife(player1, 20);

        reachEndStep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not lose life if Nim Abomination becomes tapped before its trigger resolves")
    void doesNotLoseLifeIfTappedBeforeTriggerResolves() {
        addCreatureReady(player1, new LoxodonMystic());
        Permanent nimAbomination = harness.addToBattlefieldAndReturn(player1, new NimAbomination());
        harness.setLife(player1, 20);

        reachEndStep();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, nimAbomination.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An untapped Nim Abomination leaving the battlefield does not prevent life loss")
    void losesLifeUsingLastKnownUntappedState() {
        Permanent nimAbomination = harness.addToBattlefieldAndReturn(player1, new NimAbomination());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new EchoingTruth()));

        reachEndStep();
        assertThat(gd.stack).hasSize(1);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, nimAbomination.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Nim Abomination");
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A source tapped before leaving the battlefield uses its tapped departure state")
    void doesNotLoseLifeUsingLastKnownTappedState() {
        addCreatureReady(player1, new LoxodonMystic());
        Permanent nimAbomination = harness.addToBattlefieldAndReturn(player1, new NimAbomination());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new EchoingTruth()));

        reachEndStep();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, nimAbomination.getId());
        harness.passBothPriorities();
        assertThat(nimAbomination.isTapped()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, nimAbomination.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Nim Abomination");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A tapped source creates no end-step trigger even if untapped later in that step")
    void untappingAfterEndStepBeginsDoesNotCreateTrigger() {
        Permanent nimAbomination = harness.addToBattlefieldAndReturn(player1, new NimAbomination());
        nimAbomination.tap();
        harness.setLife(player1, 20);

        reachEndStep();
        assertThat(gd.stack).isEmpty();
        nimAbomination.untap();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }
}
