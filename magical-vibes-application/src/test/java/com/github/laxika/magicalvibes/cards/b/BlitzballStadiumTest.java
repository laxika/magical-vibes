package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlitzballStadium.class, Forest.class, GrizzlyBears.class})
class BlitzballStadiumTest extends BaseCardTest {

    @Test
    void supportsUpToXTargetCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlitzballStadium()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, 2);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void makesTargetUnblockableAndDrawsForEachCounterKind() {
        Permanent stadium = harness.addToBattlefieldAndReturn(player1, new BlitzballStadium());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        attacker.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(stadium.isTapped()).isTrue();
        assertThat(attacker.isCantBeBlocked()).isTrue();

        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        attacker.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    void cannotTargetNonCreature() {
        Permanent stadium = harness.addToBattlefieldAndReturn(player1, new BlitzballStadium());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
        assertThat(stadium.isTapped()).isFalse();
    }

    @Test
    void supportWithZeroXDoesNotPutCountersOnCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlitzballStadium()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castArtifact(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Blitzball Stadium");
    }

    @Test
    void supportAllowsMoreThanOneHundredTargetsWhenXIsLarger() {
        List<Permanent> creatures = IntStream.range(0, 101)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()))
                .toList();
        harness.setHand(player1, List.of(new BlitzballStadium()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 101);

        harness.castArtifact(player1, 0, 101);
        harness.passBothPriorities();
        for (Permanent creature : creatures) {
            harness.handlePermanentChosen(player1, creature.getId());
        }
        harness.passBothPriorities();

        assertThat(creatures).allSatisfy(creature ->
                assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }

    @Test
    void creatureWithNoCountersDrawsNoCards() {
        harness.addToBattlefield(player1, new BlitzballStadium());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN, () -> {
            harness.activateAbility(player1, 0, null, attacker.getId());
            harness.passBothPriorities();
        });
        attacker.setAttacking(true);
        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.COMBAT_DAMAGE, this::resolveCombat);
        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.COMBAT_DAMAGE,
                harness::passBothPriorities);

        assertThat(attacker.isCantBeBlocked()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    void countsCounterKindsWhenDrawTriggerResolves() {
        harness.addToBattlefield(player1, new BlitzballStadium());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN, () -> {
            harness.activateAbility(player1, 0, null, attacker.getId());
            harness.passBothPriorities();
        });
        attacker.setAttacking(true);
        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.COMBAT_DAMAGE, this::resolveCombat);
        attacker.setCounterCount(CounterType.CHARGE, 3);
        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.COMBAT_DAMAGE,
                harness::passBothPriorities);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    void opponentCreatureControllerDrawsInsteadOfStadiumController() {
        harness.addToBattlefield(player1, new BlitzballStadium());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int player1HandSize = gd.playerHands.get(player1.getId()).size();
        int player2HandSize = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        attacker.setAttacking(true);
        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandSize);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandSize + 1);
    }
}
