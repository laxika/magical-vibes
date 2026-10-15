package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReapingWillow.class, GrizzlyBears.class, AngelOfMercy.class})
class ReapingWillowTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two -1/-1 counters")
    void entersWithMinusOneMinusOneCounters() {
        Permanent willow = castWillow();

        assertThat(willow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removes two counters and returns a creature with mana value 3 or less")
    void removesCountersAndReanimatesCreature() {
        Permanent willow = castWillow();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 2);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(willow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can remove any counters, not only -1/-1 counters")
    void removesGenericCounters() {
        Permanent willow = castWillow();
        willow.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        willow.setCounterCount(CounterType.CHARGE, 2);
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 2);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(willow.getCounterCount(CounterType.CHARGE)).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Rejects a creature card with mana value greater than 3")
    void rejectsHighManaValueCreature() {
        Permanent willow = castWillow();
        Card target = new AngelOfMercy();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 2);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(willow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    void rejectsInsufficientCounters() {
        Permanent willow = castWillow();
        willow.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 2);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(willow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rejectsOpponentsGraveyard() {
        Permanent willow = castWillow();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 2);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(willow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    void rejectsActivationOutsideMainPhase() {
        Permanent willow = castWillow();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 2);
        enterMainWithPriority(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(willow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    void paysCountersImmediatelyAndDoesNotReturnDepartedTarget() {
        Permanent willow = castWillow();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 2);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD);
        assertThat(willow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(willow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    void removesCountersOfDifferentKindsToPayCost() {
        Permanent willow = castWillow();
        willow.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        willow.setCounterCount(CounterType.CHARGE, 1);
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 2);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(willow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(willow.getCounterCount(CounterType.CHARGE)).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
    @Test
    void rejectsActivationDuringOpponentsTurn() {
        Permanent willow = castWillow();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 2);
        enterMainWithPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(willow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    void rejectsActivationWithNonemptyStack() {
        Permanent willow = castWillow();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        enterMainWithPriority(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(willow.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        harness.passBothPriorities();
    }
    private void enterMainWithPriority(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent castWillow() {
        harness.setHand(player1, List.of(new ReapingWillow()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Reaping Willow");
    }
}
