package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class DemonicJunkerTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys up to one creature per player and grows when your creature dies")
    void destroysOneCreaturePerPlayerAndGrowsWhenOwnCreatureDies() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castJunker(List.of(ownCreature.getId(), opposingCreature.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        Permanent junker = findPermanent(player1, "Demonic Junker");
        assertThat(junker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not grow when only an opposing creature is destroyed")
    void doesNotGrowWhenOnlyOpposingCreatureDies() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castJunker(List.of(opposingCreature.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanent(player1, "Demonic Junker")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Allows choosing no creatures")
    void allowsChoosingNoCreatures() {
        castJunker(List.of());

        assertThat(findPermanent(player1, "Demonic Junker")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Allows at most one chosen creature per player")
    void allowsAtMostOneCreaturePerPlayer() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareJunkerCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0,
                List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");
    }

    private void castJunker(List<UUID> targetIds) {
        prepareJunkerCast();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareJunkerCast() {
        harness.setHand(player1, List.of(new DemonicJunker()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }
}
