package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DinosaursOnASpaceship;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RotatingFireplace.class, DinosaursOnASpaceship.class})
class RotatingFireplaceTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped with a time counter")
    void entersTappedWithTimeCounter() {
        harness.setHand(player1, List.of(new RotatingFireplace()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent fireplace = findPermanent(player1, "Rotating Fireplace");
        assertThat(fireplace.isTapped()).isTrue();
        assertThat(fireplace.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    @DisplayName("Adds colorless mana equal to its time counters")
    void addsColorlessManaEqualToTimeCounters() {
        Permanent fireplace = harness.addToBattlefieldAndReturn(player1, new RotatingFireplace());
        fireplace.setCounterCount(CounterType.TIME, 3);
        fireplace.untap();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(fireplace.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Time travels when its sorcery-speed ability resolves")
    void timeTravels() {
        Permanent fireplace = harness.addToBattlefieldAndReturn(player1, new RotatingFireplace());
        fireplace.setCounterCount(CounterType.TIME, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ADD");

        assertThat(fireplace.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    void producesNoManaWithoutTimeCounters() {
        Permanent fireplace = harness.addToBattlefieldAndReturn(player1, new RotatingFireplace());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(fireplace.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void choosesIndependentlyAndIgnoresOpponentsAndPermanentsWithoutTimeCounters() {
        Permanent fireplace = harness.addToBattlefieldAndReturn(player1, new RotatingFireplace());
        fireplace.setCounterCount(CounterType.TIME, 1);
        Permanent other = harness.addToBattlefieldAndReturn(player1, new RotatingFireplace());
        other.setCounterCount(CounterType.TIME, 2);
        Permanent withoutCounters = harness.addToBattlefieldAndReturn(player1, new RotatingFireplace());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new RotatingFireplace());
        opponent.setCounterCount(CounterType.TIME, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(fireplace.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(fireplace.getCounterCount(CounterType.TIME)).isEqualTo(1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "REMOVE");
        harness.handleListChoice(player1, "SKIP");

        assertThat(fireplace.getCounterCount(CounterType.TIME)).isZero();
        assertThat(other.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(withoutCounters.getCounterCount(CounterType.TIME)).isZero();
        assertThat(opponent.getCounterCount(CounterType.TIME)).isEqualTo(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void changesOnlyOwnedSuspendedCards() {
        Permanent fireplace = harness.addToBattlefieldAndReturn(player1, new RotatingFireplace());
        DinosaursOnASpaceship owned = new DinosaursOnASpaceship();
        DinosaursOnASpaceship opponent = new DinosaursOnASpaceship();
        harness.setExile(player1, List.of(owned));
        harness.setExile(player2, List.of(opponent));
        gd.exiledCardTimeCounters.put(owned.getId(), 2);
        gd.exiledCardTimeCounters.put(opponent.getId(), 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ADD");

        assertThat(gd.exiledCardTimeCounters).containsEntry(owned.getId(), 3)
                .containsEntry(opponent.getId(), 2);
        assertThat(fireplace.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void removesTimeCounterFromSuspendedCardAndTriggersItsAbility() {
        harness.addToBattlefield(player1, new RotatingFireplace());
        DinosaursOnASpaceship suspended = new DinosaursOnASpaceship();
        harness.setExile(player1, List.of(suspended));
        gd.exiledCardTimeCounters.put(suspended.getId(), 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "REMOVE");
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(suspended.getId(), 1);
        assertThat(countPermanents(player1, "Dinosaur")).isEqualTo(1);
    }

    @Test
    void resolvesWithoutChoicesWhenNothingHasTimeCounters() {
        harness.addToBattlefield(player1, new RotatingFireplace());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTimeTravelOutsideMainPhase() {
        Permanent fireplace = harness.addToBattlefieldAndReturn(player1, new RotatingFireplace());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(fireplace.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
    }

    @Test
    void cannotTimeTravelOnOpponentsTurn() {
        Permanent fireplace = harness.addToBattlefieldAndReturn(player1, new RotatingFireplace());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(fireplace.isTapped()).isFalse();
    }

    @Test
    void cannotTimeTravelWithNonemptyStack() {
        harness.addToBattlefield(player1, new RotatingFireplace());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new RotatingFireplace());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(other.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
    }
}
