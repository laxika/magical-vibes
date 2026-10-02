package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({AgonasaurRex.class, DuskLegionDreadnought.class, Forest.class, GrizzlyBears.class})
class AgonasaurRexTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling puts two counters on a creature and grants trample and indestructible")
    void cyclingBoostsCreatureAndDraws() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cycleAgonasaurRex(bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertInGraveyard(player1, "Agonasaur Rex");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling can target a noncreature Vehicle")
    void cyclingBoostsVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new DuskLegionDreadnought());

        cycleAgonasaurRex(vehicle);

        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(vehicle.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(vehicle.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Cycling with no target still draws a card")
    void cyclingWithNoTargetStillDraws() {
        harness.setHand(player1, List.of(new AgonasaurRex()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Agonasaur Rex");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling cannot target a noncreature non-Vehicle permanent")
    void cyclingCannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new AgonasaurRex()));
        addCyclingMana();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Granted keywords wear off at end of turn but counters remain")
    void grantedKeywordsWearOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cycleAgonasaurRex(bears);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The cycling trigger resolves before the separate cycling draw")
    void cyclingTriggerResolvesBeforeDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AgonasaurRex());
        harness.setHand(player1, List.of(new AgonasaurRex()));
        harness.setLibrary(player1, List.of(new Forest()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, target.getId());
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, target.getId());
        }
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertNotInHand(player1, "Forest");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("An illegal cycling-trigger target does not prevent the cycling draw")
    void cyclingStillDrawsWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AgonasaurRex());
        harness.setHand(player1, List.of(new AgonasaurRex()));
        harness.setLibrary(player1, List.of(new Forest()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, target.getId());
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, target.getId());
        }
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, target));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Agonasaur Rex");
        harness.assertInHand(player1, "Forest");
    }

    private void cycleAgonasaurRex(Permanent target) {
        harness.setHand(player1, List.of(new AgonasaurRex()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addCyclingMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
