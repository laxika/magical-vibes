package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.n.NoggleRobber;
import com.github.laxika.magicalvibes.cards.n.NoviceInspector;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({GimbalGremlinProdigy.class, Ornithopter.class, GrizzlyBears.class, NoviceInspector.class,
        NoggleRobber.class})
class GimbalGremlinProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact creatures you control have trample")
    void grantsTrampleToOwnArtifactCreatures() {
        harness.addToBattlefield(player1, new GimbalGremlinProdigy());
        Permanent ownArtifactCreature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingArtifactCreature = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        assertThat(gqs.hasKeyword(gd, ownArtifactCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingArtifactCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Creates a Gremlin with one counter per differently named artifact token")
    void createsGremlinWithDistinctArtifactTokenNameCounters() {
        createClueAndTwoTreasures();
        harness.addToBattlefield(player1, new GimbalGremlinProdigy());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        Permanent gremlin = findPermanent(player1, "Gremlin");
        assertThat(gremlin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gremlin.getEffectivePower()).isEqualTo(2);
        assertThat(gremlin.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.isArtifact(gd, gremlin)).isTrue();
        assertThat(gqs.hasKeyword(gd, gremlin, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Counts artifact tokens but not ordinary artifact permanents")
    void ignoresNonTokenArtifactsInCounterCount() {
        harness.addToBattlefield(player1, new Ornithopter());
        createClue();
        harness.addToBattlefield(player1, new GimbalGremlinProdigy());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        Permanent gremlin = findPermanent(player1, "Gremlin");
        assertThat(gremlin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void createClue() {
        harness.setHand(player1, List.of(new NoviceInspector()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void createClueAndTwoTreasures() {
        harness.setHand(player1, List.of(new NoviceInspector(), new NoggleRobber(), new NoggleRobber()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 6);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
