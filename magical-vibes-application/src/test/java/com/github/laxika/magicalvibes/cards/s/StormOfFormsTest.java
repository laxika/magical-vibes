package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormOfForms.class, Forest.class, GrizzlyBears.class})
class StormOfFormsTest extends BaseCardTest {

    @Test
    @DisplayName("Copies once for each distinct counter kind on your permanents")
    void copiesForEachDistinctControlledCounterKind() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        second.setCounterCount(CounterType.CHARGE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castStormOfForms(target);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(2);
        assertThat(gd.stack).filteredOn(entry -> entry.getCard().getName().equals("Storm of Forms"))
                .hasSize(3);
    }

    @Test
    @DisplayName("Counts counter kinds when the cast trigger resolves")
    void countsCounterKindsAtTriggerResolution() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castStormOfForms(target);
        permanent.setCounterCount(CounterType.CHARGE, 1);
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(2);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Returns the targeted nonland permanent to its owner's hand")
    void returnsTargetToHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castStormOfForms(target);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new StormOfForms()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    @DisplayName("Does not copy for counter kinds removed before the trigger resolves")
    void ignoresCounterKindsRemovedBeforeResolution() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castStormOfForms(target);
        permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Multiple counters and permanents of the same kind create only one copy")
    void countsDuplicateKindsOnlyOnceAndIgnoresOpponentsCounters() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.CHARGE, 1);

        castStormOfForms(target);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
    }

    @Test
    @DisplayName("Counters on controlled lands count even though lands cannot be targeted")
    void countsCounterKindsOnLands() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.setCounterCount(CounterType.CHARGE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castStormOfForms(target);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
    }

    @Test
    @DisplayName("A copy may bounce a new target without changing the original spell's target")
    void copyMayChooseNewTarget() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        originalTarget.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent newTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castStormOfForms(originalTarget);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, newTarget.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Keeping the original target lets the copy bounce it and the original fail to resolve")
    void copyKeepsTargetWhenRetargetingIsDeclined() {
        Permanent counterSource = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        counterSource.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castStormOfForms(target);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears")).hasSize(1);
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Storm of Forms");
        assertThat(gd.stack).isEmpty();
    }

    private void castStormOfForms(Permanent target) {
        harness.setHand(player1, List.of(new StormOfForms()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castInstant(player1, 0, target.getId());
    }
}
