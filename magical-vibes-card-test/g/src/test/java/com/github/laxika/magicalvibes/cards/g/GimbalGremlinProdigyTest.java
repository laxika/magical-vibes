package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.n.NoggleRobber;
import com.github.laxika.magicalvibes.cards.n.NoviceInspector;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

        Permanent gremlin = gremlins(player1).getFirst();
        assertThat(gremlin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gremlin.getEffectivePower()).isEqualTo(3);
        assertThat(gremlin.getEffectiveToughness()).isEqualTo(3);
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

        Permanent gremlin = gremlins(player1).getFirst();
        assertThat(gremlin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The newly created Gremlin counts itself even with no preexisting artifact tokens")
    void firstGremlinSurvivesWithOneCounter() {
        harness.addToBattlefield(player1, new GimbalGremlinProdigy());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gremlins(player1)).hasSize(1);
        assertThat(gremlins(player1).getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated Gremlins share a name and do not increase the distinct-name count")
    void repeatedGremlinsCountAsOneName() {
        harness.addToBattlefield(player1, new GimbalGremlinProdigy());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gremlins(player1)).hasSize(2).allSatisfy(gremlin ->
                assertThat(gremlin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }

    @Test
    @DisplayName("Gimbal does not trigger at an opponent's end step")
    void doesNotCreateGremlinOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new GimbalGremlinProdigy());

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(gremlins(player1)).isEmpty();
        assertThat(gremlins(player2)).isEmpty();
    }

    @Test
    @DisplayName("Artifact tokens controlled by an opponent are not counted")
    void ignoresOpponentsArtifactTokens() {
        harness.addToBattlefield(player2, new GimbalGremlinProdigy());
        advanceToEndStep(player2);
        harness.passBothPriorities();
        assertThat(gremlins(player2)).hasSize(1);
        harness.addToBattlefield(player1, new GimbalGremlinProdigy());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gremlins(player1)).hasSize(1);
        assertThat(gremlins(player1).getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The generated Gremlin has the default name Gremlin Token")
    void createsGremlinWithDefaultTokenName() {
        createClue();
        harness.addToBattlefield(player1, new GimbalGremlinProdigy());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gremlins(player1)).hasSize(1);
        assertThat(gremlins(player1).getFirst().getCard().getName()).isEqualTo("Gremlin Token");
    }

    private List<Permanent> gremlins(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.GREMLIN))
                .toList();
    }

    private void createClue() {
        harness.setHand(player1, List.of(new NoviceInspector()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private void createClueAndTwoTreasures() {
        harness.setHand(player1, List.of(new NoviceInspector(), new NoggleRobber(), new NoggleRobber()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 6);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.END_STEP);
    }
}
