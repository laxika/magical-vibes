package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MutantsPreyTest extends BaseCardTest {

    @Test
    @DisplayName("Countered creature you control fights opponent creature and kills it")
    void counteredCreatureFightsAndKills() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new MutantsPrey()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // 3/3

        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(bear.getId(), elvesId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bear.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Both creatures die when the fight is mutually lethal")
    void bothCreaturesDie() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new MutantsPrey()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // 3/3

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, List.of(bear.getId(), giantId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Cannot choose a creature you control without a +1/+1 counter")
    void cannotTargetCreatureWithoutCounter() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new MutantsPrey()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bearId, elvesId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose your own creature as the second target")
    void cannotTargetOwnCreatureAsSecondTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new MutantsPrey()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        UUID elvesId = harness.getPermanentId(player1, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bear.getId(), elvesId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Neither creature deals damage when the opponent's creature leaves before resolution")
    void neitherFightsWhenSecondTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new MutantsPrey()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(bear.getId(), elvesId));

        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bear.getMarkedDamage()).isZero();
    }
}
