package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
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

@CardUsed({ClayChampion.class, ArgothianSprite.class})
class ClayChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three counters per green pair and counters up to two other creatures per white pair")
    void scalesCountersFromColoredManaPairs() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.setHand(player1, List.of(new ClayChampion()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 6, null, null, List.of(first.getId(), second.getId()), List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent champion = findPermanent(player1, "Clay Champion");
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Rejects an opponent's creature as an ETB target")
    void cannotTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        harness.setHand(player1, List.of(new ClayChampion()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature you control");
    }

    @Test
    void roundsDownOddGreenAndWhiteManaIndependently() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.setHand(player1, List.of(new ClayChampion()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.WHITE, 5);

        gs.playCard(gd, player1, 0, 4, null, null, List.of(target.getId()), List.of());
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Clay Champion")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void canChooseNoTargetsEvenWhenOtherCreaturesAreAvailable() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.setHand(player1, List.of(new ClayChampion()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Clay Champion")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void colorlessManaDoesNotProduceEitherKindOfCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.setHand(player1, List.of(new ClayChampion()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Clay Champion")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotChooseMoreThanTwoOtherCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.setHand(player1, List.of(new ClayChampion()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enteringWithoutBeingCastDoesNotUseManaInThePool() {
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.WHITE, 4);

        Permanent champion = harness.enterBattlefieldAndReturn(player1, new ClayChampion());

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
