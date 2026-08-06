package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TortureChamberTest extends BaseCardTest {

    @Test
    @DisplayName("A pain counter is added at the controller's upkeep")
    void upkeepAddsPainCounter() {
        harness.addToBattlefield(player1, new TortureChamber());
        Permanent chamber = findPermanent(player1, "Torture Chamber");

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger

        assertThat(chamber.getCounterCount(CounterType.PAIN)).isEqualTo(1);
    }

    @Test
    @DisplayName("At the controller's end step it deals damage equal to its pain counters")
    void endStepDealsDamageEqualToCounters() {
        harness.addToBattlefield(player1, new TortureChamber());
        Permanent chamber = findPermanent(player1, "Torture Chamber");
        chamber.setCounterCount(CounterType.PAIN, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities(); // advance to end step -> trigger queued
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        harness.passBothPriorities(); // resolve trigger

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        assertThat(chamber.getCounterCount(CounterType.PAIN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not damage its controller during an opponent's end step")
    void doesNotTriggerDuringOpponentEndStep() {
        harness.addToBattlefield(player1, new TortureChamber());
        findPermanent(player1, "Torture Chamber").setCounterCount(CounterType.PAIN, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Activating removes all pain counters as a cost and deals that much damage to target creature")
    void activatedAbilityDealsDamageEqualToCountersRemoved() {
        Permanent chamber = addReadyChamber(player1);
        chamber.setCounterCount(CounterType.PAIN, 2);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, indexOf(player1, chamber), null, bearsId);

        assertThat(chamber.getCounterCount(CounterType.PAIN)).isZero();

        harness.passBothPriorities(); // resolve ability

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("With one pain counter the target creature survives with 1 damage marked")
    void oneCounterDealsOneDamage() {
        Permanent chamber = addReadyChamber(player1);
        chamber.setCounterCount(CounterType.PAIN, 1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, indexOf(player1, chamber), null, bearsId);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent chamber = addReadyChamber(player1);
        chamber.setCounterCount(CounterType.PAIN, 2);
        Permanent otherChamber = addReadyChamber(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, chamber), null, otherChamber.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability requires tapping — a tapped Torture Chamber cannot activate")
    void activatedAbilityRequiresTap() {
        Permanent chamber = addReadyChamber(player1);
        chamber.setCounterCount(CounterType.PAIN, 2);
        chamber.tap();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, chamber), null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private Permanent addReadyChamber(Player player) {
        Permanent permanent = new Permanent(new TortureChamber());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
