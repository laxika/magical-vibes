package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    @DisplayName("Returning permanents sacrifices Recantation")
    void returnsUpToVerseCounterPermanents() {
        Permanent recantation = harness.addToBattlefieldAndReturn(player1, new Recantation());
        recantation.setCounterCount(CounterType.VERSE, 2);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId(), land.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Island");
        harness.assertInGraveyard(player1, "Recantation");
    }

    @Test
    @DisplayName("Cannot choose more targets than verse counters")
    void cannotChooseMoreTargetsThanVerseCounters() {
        Permanent recantation = harness.addToBattlefieldAndReturn(player1, new Recantation());
        recantation.setCounterCount(CounterType.VERSE, 1);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Recantation");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
