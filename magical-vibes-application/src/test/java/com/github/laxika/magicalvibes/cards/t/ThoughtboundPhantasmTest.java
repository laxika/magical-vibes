package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DazzlingLights;
import com.github.laxika.magicalvibes.cards.g.GenerousStray;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThoughtboundPhantasm.class, DazzlingLights.class, GenerousStray.class})
class ThoughtboundPhantasmTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever its controller surveils, it gets a +1/+1 counter")
    void surveilingAddsCounter() {
        Permanent phantasm = addCreatureReady(player1, new ThoughtboundPhantasm());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GenerousStray());
        harness.setLibrary(player1, List.of(new GenerousStray(), new GenerousStray()));
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));
        harness.passBothPriorities();

        assertThat(phantasm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("It cannot attack with fewer than three +1/+1 counters")
    void cannotAttackBelowThreshold() {
        addCreatureReady(player1, new ThoughtboundPhantasm());
        harness.addToBattlefield(player2, new GenerousStray());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("It can attack with three or more +1/+1 counters despite defender")
    void canAttackAtThreshold() {
        Permanent phantasm = addCreatureReady(player1, new ThoughtboundPhantasm());
        phantasm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addToBattlefield(player2, new GenerousStray());
        int phantasmIndex = gd.playerBattlefields.get(player1.getId()).indexOf(phantasm);

        declareAttackers(List.of(phantasmIndex));

        assertThat(phantasm.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Keeping all surveilled cards still adds one counter after surveilling finishes")
    void keepingAllCardsStillAddsCounter() {
        Permanent phantasm = addCreatureReady(player1, new ThoughtboundPhantasm());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GenerousStray());
        harness.setLibrary(player1, List.of(new GenerousStray(), new GenerousStray()));
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(phantasm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        resolveAllTriggers();

        assertThat(phantasm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An opponent's surveil does not add a counter")
    void opponentSurveilDoesNotAddCounter() {
        Permanent phantasm = addCreatureReady(player1, new ThoughtboundPhantasm());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GenerousStray());
        harness.setLibrary(player2, List.of(new GenerousStray(), new GenerousStray()));
        harness.setHand(player2, List.of(new DazzlingLights()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, target.getId());
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));
        resolveAllTriggers();

        assertThat(phantasm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Surveilling an empty library still adds a counter")
    void surveilingEmptyLibraryAddsCounter() {
        Permanent phantasm = addCreatureReady(player1, new ThoughtboundPhantasm());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GenerousStray());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(phantasm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dropping below three counters before attacking restores the defender restriction")
    void losingCountersBeforeAttackingRestoresRestriction() {
        Permanent phantasm = addCreatureReady(player1, new ThoughtboundPhantasm());
        phantasm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        assertThat(als.canAttack(gd, phantasm, player1.getId())).isTrue();
        phantasm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Losing counters after attacking does not remove it from combat")
    void losingCountersAfterAttackingKeepsItInCombat() {
        Permanent phantasm = addCreatureReady(player1, new ThoughtboundPhantasm());
        phantasm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addToBattlefield(player2, new GenerousStray());

        declareAttackersAndPrepareBlockers(List.of(0));
        phantasm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThat(phantasm.isAttacking()).isTrue();
    }
}
