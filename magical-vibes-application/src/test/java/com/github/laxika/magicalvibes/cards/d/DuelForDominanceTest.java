package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MirriCatWarrior;
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

class DuelForDominanceTest extends BaseCardTest {

    @Test
    @DisplayName("Without coven: creatures fight with no +1/+1 counter")
    void withoutCovenCreaturesFightWithoutCounter() {
        // Only one creature you control — coven not met
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new DuelForDominance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(bearId, elvesId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("With coven: +1/+1 counter on your creature before fight")
    void withCovenPutsCounterBeforeFight() {
        // Elves 1/1, Bears 2/2, Giant 3/3 — three different powers = coven
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new DuelForDominance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID theirElvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(bearId, theirElvesId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        Permanent bear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> "Grizzly Bears".equals(p.getCard().getName()))
                .findFirst()
                .orElseThrow();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Coven counter helps creature survive a fight it would otherwise lose")
    void covenCounterHelpsCreatureSurvive() {
        // Mirri 2/3 vs Giant 3/3: without counter Mirri dies; with counter Mirri is 3/4 and both trade
        // Board powers 1/2/3 (Elves/Mirri/Hill Giant) meet coven
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new MirriCatWarrior());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new DuelForDominance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID mirriId = harness.getPermanentId(player1, "Mirri, Cat Warrior");
        UUID theirGiantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, List.of(mirriId, theirGiantId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mirri, Cat Warrior");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        Permanent mirri = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> "Mirri, Cat Warrior".equals(p.getCard().getName()))
                .findFirst()
                .orElseThrow();
        assertThat(mirri.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target opponent's creature as first target")
    void cannotTargetOpponentCreatureAsFirstTarget() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new DuelForDominance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID theirBearId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID theirElvesId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(theirBearId, theirElvesId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Cannot target own creature as second target")
    void cannotTargetOwnCreatureAsSecondTarget() {
        GrizzlyBears bear1 = new GrizzlyBears();
        GrizzlyBears bear2 = new GrizzlyBears();
        harness.addToBattlefield(player1, bear1);
        harness.addToBattlefield(player1, bear2);
        harness.setHand(player1, List.of(new DuelForDominance()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(id1, id2)))
                .isInstanceOf(IllegalStateException.class);
    }
}
