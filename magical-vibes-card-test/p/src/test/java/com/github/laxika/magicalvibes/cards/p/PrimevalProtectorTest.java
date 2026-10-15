package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrimevalProtector.class, GrizzlyBears.class, SolRing.class})
class PrimevalProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less to cast for each creature an opponent controls")
    void costReductionCountsOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PrimevalProtector()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Creatures you control do not reduce its cost")
    void costReductionIgnoresYourCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PrimevalProtector()));
        harness.addMana(player1, ManaColor.GREEN, 10);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("ETB puts a +1/+1 counter on each other creature you control")
    void etbBoostsOtherControlledCreatures() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PrimevalProtector()));
        harness.addMana(player1, ManaColor.GREEN, 10);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent protector = findPermanent(player1, "Primeval Protector");
        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(protector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void excessOpponentCreaturesReduceCostToOneGreen() {
        for (int i = 0; i < 12; i++) {
            harness.addToBattlefield(player2, new PrimevalProtector());
        }
        harness.setHand(player1, List.of(new PrimevalProtector()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void costReductionDoesNotRemoveGreenRequirement() {
        for (int i = 0; i < 12; i++) {
            harness.addToBattlefield(player2, new PrimevalProtector());
        }
        harness.setHand(player1, List.of(new PrimevalProtector()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void opponentNoncreaturesDoNotReduceCost() {
        harness.addToBattlefield(player2, new SolRing());
        harness.setHand(player1, List.of(new PrimevalProtector()));
        harness.addMana(player1, ManaColor.GREEN, 10);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void triggerUsesCreaturesPresentAtResolutionAndExcludesOnlyItsSource() {
        Permanent earlierProtector = harness.addToBattlefieldAndReturn(player1, new PrimevalProtector());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SolRing());
        harness.setHand(player1, List.of(new PrimevalProtector()));
        harness.addMana(player1, ManaColor.GREEN, 11);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(earlierProtector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new PrimevalProtector());
        resolveAllTriggers();

        assertThat(earlierProtector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(lateCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        Permanent source = findPermanents(player1, "Primeval Protector").get(1);
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void enteringWithoutOtherCreaturesDoesNotCounterItself() {
        harness.setHand(player1, List.of(new PrimevalProtector()));
        harness.addMana(player1, ManaColor.GREEN, 11);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Primeval Protector")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
