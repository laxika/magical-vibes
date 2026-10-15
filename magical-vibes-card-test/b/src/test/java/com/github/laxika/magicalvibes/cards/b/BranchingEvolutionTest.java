package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HardenedScales;
import com.github.laxika.magicalvibes.cards.p.Pentavus;
import com.github.laxika.magicalvibes.cards.t.TimberlandGuide;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BranchingEvolution.class, GrizzlyBears.class, Pentavus.class, TimberlandGuide.class})
class BranchingEvolutionTest extends BaseCardTest {

    @Test
    @DisplayName("doubles +1/+1 counters put on a creature you control")
    void doublesCountersOnControlledCreature() {
        harness.addToBattlefield(player1, new BranchingEvolution());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, List.of(bears.getId()));
        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("does not double +1/+1 counters on a creature an opponent controls")
    void doesNotDoubleOnOpponentCreature() {
        harness.addToBattlefield(player1, new BranchingEvolution());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);

        harness.castCreature(player1, 0, List.of(bears.getId()));
        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("doubles +1/+1 counters a creature enters with")
    void doublesEnterWithCounters() {
        harness.addToBattlefield(player1, new BranchingEvolution());

        harness.castFromHand(player1, new Pentavus(), "{7}");
        harness.passBothPriorities();

        Permanent pentavus = findPermanent(player1, "Pentavus");
        assertThat(pentavus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
    }

    @Test
    @DisplayName("two copies quadruple incoming counters")
    void twoCopiesQuadrupleCounters() {
        harness.addToBattlefield(player1, new BranchingEvolution());
        harness.addToBattlefield(player1, new BranchingEvolution());

        harness.castFromHand(player1, new Pentavus(), "{7}");
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Pentavus").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(20);
    }

    @Test
    @DisplayName("doubles only incoming counters, leaving existing counters unchanged")
    void leavesExistingCountersUnchanged() {
        harness.addToBattlefield(player1, new BranchingEvolution());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0, List.of(bears.getId()));
        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Branching Evolution", "Hardened Scales"})
    @CardUsed({HardenedScales.class})
    @DisplayName("controller chooses the order of Branching Evolution and Hardened Scales")
    void controllerChoosesReplacementOrder(String firstReplacement) {
        harness.addToBattlefield(player1, new BranchingEvolution());
        harness.addToBattlefield(player1, new HardenedScales());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0, List.of(bears.getId()));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, choice.options().stream()
                .filter(option -> option.startsWith(firstReplacement)).findFirst().orElseThrow());
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(firstReplacement.equals("Branching Evolution") ? 3 : 4);
    }

    @Test
    @CardUsed(HardenedScales.class)
    void canDoubleBetweenTwoIndependentCounterIncreases() {
        harness.addToBattlefield(player1, new BranchingEvolution());
        harness.addToBattlefield(player1, new HardenedScales());
        harness.addToBattlefield(player1, new HardenedScales());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0, List.of(bears.getId()));
        resolveAllTriggers();

        PendingInteraction.ColorChoice first = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, first.options().stream()
                .filter(option -> option.startsWith("Hardened Scales")).findFirst().orElseThrow());
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        PendingInteraction.ColorChoice second = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, second.options().stream()
                .filter(option -> option.startsWith("Branching Evolution")).findFirst().orElseThrow());

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }
}
