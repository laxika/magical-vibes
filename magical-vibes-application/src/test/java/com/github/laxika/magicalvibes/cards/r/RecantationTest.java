package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({Recantation.class, CoralMerfolk.class, Island.class})
class RecantationTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the upkeep trigger puts a verse counter on Recantation")
    void upkeepAcceptedAddsVerseCounter() {
        Permanent recantation = harness.addToBattlefieldAndReturn(player1, new Recantation());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(recantation.getCounterCount(CounterType.VERSE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the upkeep trigger does not put a verse counter on Recantation")
    void upkeepDeclinedDoesNotAddVerseCounter() {
        Permanent recantation = harness.addToBattlefieldAndReturn(player1, new Recantation());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(recantation.getCounterCount(CounterType.VERSE)).isZero();
    }

    @Test
    @DisplayName("Returning permanents sacrifices Recantation")
    void returnsUpToVerseCounterPermanents() {
        Permanent recantation = harness.addToBattlefieldAndReturn(player1, new Recantation());
        recantation.setCounterCount(CounterType.VERSE, 2);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId(), land.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Coral Merfolk");
        harness.assertInHand(player2, "Island");
        harness.assertInGraveyard(player1, "Recantation");
    }

    @Test
    @DisplayName("Can return fewer permanents than the number of verse counters")
    void canReturnFewerPermanentsThanVerseCounters() {
        Permanent recantation = harness.addToBattlefieldAndReturn(player1, new Recantation());
        recantation.setCounterCount(CounterType.VERSE, 2);
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(permanent.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Coral Merfolk");
        harness.assertInGraveyard(player1, "Recantation");
    }

    @Test
    @DisplayName("Can return a permanent controlled by the ability's controller")
    void canReturnOwnPermanent() {
        Permanent recantation = harness.addToBattlefieldAndReturn(player1, new Recantation());
        recantation.setCounterCount(CounterType.VERSE, 1);
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(permanent.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Coral Merfolk");
        harness.assertInGraveyard(player1, "Recantation");
    }

    @Test
    @DisplayName("Cannot choose more targets than verse counters")
    void cannotChooseMoreTargetsThanVerseCounters() {
        Permanent recantation = harness.addToBattlefieldAndReturn(player1, new Recantation());
        recantation.setCounterCount(CounterType.VERSE, 1);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between 0 and 1 targets");
    }

    @Test
    @DisplayName("With no verse counters, the ability sacrifices Recantation without returning permanents")
    void zeroVerseCountersSacrificesWithoutReturningPermanents() {
        harness.addToBattlefield(player1, new Recantation());
        harness.addToBattlefield(player2, new CoralMerfolk());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Recantation");
        harness.assertOnBattlefield(player2, "Coral Merfolk");
    }
}
