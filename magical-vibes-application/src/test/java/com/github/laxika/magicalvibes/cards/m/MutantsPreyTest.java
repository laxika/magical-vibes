package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MutantsPrey.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class})
class MutantsPreyTest extends BaseCardTest {

    @Test
    @DisplayName("Countered creature you control fights opponent creature and kills it")
    void counteredCreatureFightsAndKills() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new MutantsPrey()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // 3/3

        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveInstant(player1, 0, List.of(bear.getId(), elvesId));

        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bear.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Both creatures die when the fight is mutually lethal")
    void bothCreaturesDie() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new MutantsPrey()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // 3/3

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castAndResolveInstant(player1, 0, List.of(bear.getId(), giantId));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Cannot choose a creature you control without a +1/+1 counter")
    void cannotTargetCreatureWithoutCounter() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new MutantsPrey()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = bear.getId();
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bearId, elvesId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose your own creature as the second target")
    void cannotTargetOwnCreatureAsSecondTarget() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new MutantsPrey()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        UUID elvesId = harness.getPermanentId(player1, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bear.getId(), elvesId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Neither creature deals damage when the opponent's creature leaves before resolution")
    void neitherFightsWhenSecondTargetRemoved() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new MutantsPrey()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(bear.getId(), elvesId));

        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bear.getMarkedDamage()).isZero();
    }
    @Test
    @DisplayName("Neither creature fights if the required counter is removed before resolution")
    void neitherFightsAfterLosingCounter() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new MutantsPrey()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, List.of(bear.getId(), elves.getId()));
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(bear.getMarkedDamage()).isZero();
        assertThat(elves.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Mutant's Prey");
    }

    @Test
    @DisplayName("Neither creature fights if the second target becomes controlled by you")
    void neitherFightsAfterSecondTargetChangesController() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new MutantsPrey()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, List.of(bear.getId(), elves.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(elves);
        gd.playerBattlefields.get(player1.getId()).add(elves);
        gd.stolenCreatures.put(elves.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(bear.getMarkedDamage()).isZero();
        assertThat(elves.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot choose an opponent's countered creature as the first target")
    void cannotTargetOpponentsCreatureAsFirstTarget() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new MutantsPrey()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bear.getId(), elves.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
