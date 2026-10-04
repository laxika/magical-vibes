package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinRazerunners.class, Forest.class, NicolBolasPlaneswalker.class, Unsummon.class})
class GoblinRazerunnersTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices a land and puts a +1/+1 counter on this creature")
    void activatingSacrificesLandAndAddsCounter() {
        Permanent razerunners = harness.addToBattlefieldAndReturn(player1, new GoblinRazerunners());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.RED, 2);

        // Only one land on the battlefield → auto-sacrificed as cost.
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(razerunners.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Deals damage equal to the number of +1/+1 counters to the chosen opponent")
    void dealsDamageEqualToCounters() {
        Permanent razerunners = harness.addToBattlefieldAndReturn(player1, new GoblinRazerunners());
        razerunners.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setLife(player2, 20);

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Any player is a legal target — controller may be chosen")
    void canTargetController() {
        Permanent razerunners = harness.addToBattlefieldAndReturn(player1, new GoblinRazerunners());
        razerunners.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player1, 20);

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities(); // resolve trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declining the may ability deals no damage")
    void decliningDealsNoDamage() {
        Permanent razerunners = harness.addToBattlefieldAndReturn(player1, new GoblinRazerunners());
        razerunners.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setLife(player2, 20);

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve trigger -> may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Sacrificing a land is an immediate cost, while the counter waits for resolution")
    void sacrificeIsPaidBeforeCounterIsAdded() {
        Permanent razerunners = harness.addToBattlefieldAndReturn(player1, new GoblinRazerunners());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Forest");
        assertThat(razerunners.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(razerunners.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's land cannot pay the sacrifice cost")
    void cannotActivateWithoutOwnLand() {
        harness.addToBattlefield(player1, new GoblinRazerunners());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability does not trigger at the opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent razerunners = harness.addToBattlefieldAndReturn(player1, new GoblinRazerunners());
        razerunners.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("With no counters the trigger still targets but deals no damage")
    void zeroCountersStillTriggers() {
        harness.addToBattlefield(player1, new GoblinRazerunners());

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Planeswalkers are legal targets but creatures and lands are not")
    void damagesPlaneswalker() {
        Permanent razerunners = harness.addToBattlefieldAndReturn(player1, new GoblinRazerunners());
        razerunners.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent bolas = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        bolas.setCounterCount(CounterType.LOYALTY, 5);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bolas.getId(), player1.getId(), player2.getId())
                .doesNotContain(razerunners.getId(), land.getId());
        harness.handlePermanentChosen(player1, bolas.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Damage counts counters added after the end-step trigger went on the stack")
    void countsCountersAtResolution() {
        Permanent razerunners = harness.addToBattlefieldAndReturn(player1, new GoblinRazerunners());
        razerunners.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addToBattlefield(player1, new Forest());

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(razerunners.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("The trigger uses last known counters when the creature leaves the battlefield")
    void dealsDamageAfterSourceLeaves() {
        Permanent razerunners = harness.addToBattlefieldAndReturn(player1, new GoblinRazerunners());
        razerunners.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new Unsummon()));

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, razerunners.getId());
        harness.assertInHand(player1, "Goblin Razerunners");
        harness.assertNotOnBattlefield(player1, "Goblin Razerunners");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 17);
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player, TurnStep.END_STEP);
    }
}
