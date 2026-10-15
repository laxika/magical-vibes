package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArashinCleric;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CitadelSiege.class, ArashinCleric.class})
class CitadelSiegeTest extends BaseCardTest {

    private Permanent castSiege(String mode) {
        harness.castFromHand(player1, new CitadelSiege(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
        return findPermanent(player1, "Citadel Siege");
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    @DisplayName("Khans puts two +1/+1 counters on a creature you control during your combat")
    void khansPutsCountersOnOwnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ArashinCleric());
        castSiege("Khans");

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Khans does not trigger during an opponent's combat")
    void khansDoesNotTriggerDuringOpponentsCombat() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ArashinCleric());
        castSiege("Khans");

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Dragons taps a creature controlled by the active opponent")
    void dragonsTapsActiveOpponentsCreature() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new ArashinCleric());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new ArashinCleric());
        castSiege("Dragons");

        advanceToCombat(player2);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownBears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opposingBears.getId());
        harness.passBothPriorities();

        assertThat(opposingBears.isTapped()).isTrue();
        assertThat(ownBears.isTapped()).isFalse();
    }
    @Test
    @DisplayName("Khans rejects opposing creatures and noncreature permanents")
    void khansRejectsIllegalTargets() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new ArashinCleric());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new ArashinCleric());
        Permanent siege = castSiege("Khans");

        advanceToCombat(player1);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, siege.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Dragons does not trigger during its controller's combat")
    void dragonsDoesNotTriggerDuringOwnCombat() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArashinCleric());
        castSiege("Dragons");

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Khans has no legal target when only an opponent controls creatures")
    void khansWithNoOwnCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArashinCleric());
        castSiege("Khans");

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Dragons has no legal target when the active opponent controls no creatures")
    void dragonsWithNoOpposingCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArashinCleric());
        castSiege("Dragons");

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Dragons may target a creature that is already tapped")
    void dragonsCanTargetTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArashinCleric());
        creature.tap();
        castSiege("Dragons");

        advanceToCombat(player2);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }
}
