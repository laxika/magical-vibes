package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MetropolisAngel.class, Forest.class, GrizzlyBears.class})
class MetropolisAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when at least one attacking creature has a counter")
    void drawsForCounteredAttacker() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addCreatureReady(player1, new MetropolisAngel());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draws only one card for multiple countered attackers")
    void drawsOnlyOnceForMultipleCounteredAttackers() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        addCreatureReady(player1, new MetropolisAngel());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        firstAttacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        secondAttacker.setCounterCount(CounterType.CHARGE, 1);

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when no attacking creature has a counter")
    void doesNotDrawForUncounteredAttackers() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addCreatureReady(player1, new MetropolisAngel());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws when Metropolis Angel itself attacks with a counter")
    void drawsForItsOwnCounter() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent angel = addCreatureReady(player1, new MetropolisAngel());
        angel.setCounterCount(CounterType.CHARGE, 1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Counters on a nonattacking creature do not cause a draw")
    void doesNotDrawForCounteredNonattacker() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent angel = addCreatureReady(player1, new MetropolisAngel());
        angel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent attacking with counters does not trigger the Angel")
    void doesNotDrawForOpponentAttack() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addCreatureReady(player1, new MetropolisAngel());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Removing the attacker's counters after triggering does not prevent the draw")
    void drawsAfterAttackerLosesCounters() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addCreatureReady(player1, new MetropolisAngel());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Adding a counter after attackers are declared does not create a draw trigger")
    void doesNotDrawForCounterAddedAfterDeclaration() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addCreatureReady(player1, new MetropolisAngel());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
