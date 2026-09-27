package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FavorableWinds;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KineticOoze.class, FavorableWinds.class, GrizzlyBears.class, Millstone.class, SolRing.class})
class KineticOozeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X counters and destroys a matching artifact or enchantment")
    void entersWithCountersAndDestroysTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        harness.setHand(player1, List.of(new KineticOoze()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 4, null, null, List.of(artifact.getId()), List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findOoze(player1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(p -> p.getCard().getName())
                .doesNotContain("Millstone");
    }

    @Test
    @DisplayName("Draws a card when X is at least five")
    void drawsAtFive() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new FavorableWinds());
        harness.setLibrary(player1, List.of(new SolRing()));
        harness.setHand(player1, List.of(new KineticOoze()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        gs.playCard(gd, player1, 0, 5, null, null, List.of(enchantment.getId()), List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Sol Ring");
    }

    @Test
    @DisplayName("At X ten, doubles +1/+1 counters on any number of other creatures")
    void doublesOtherCreatureCountersAtTen() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());

        harness.setHand(player1, List.of(new KineticOoze()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        gs.playCard(gd, player1, 0, 10, null, null,
                List.of(artifact.getId(), first.getId(), second.getId()), List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Rejects a creature as the artifact or enchantment target")
    void rejectsInvalidDestructionTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KineticOoze()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 4, null, null,
                List.of(creature.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent findOoze(com.github.laxika.magicalvibes.model.Player player) {
        return findPermanent(player, "Kinetic Ooze");
    }
}
