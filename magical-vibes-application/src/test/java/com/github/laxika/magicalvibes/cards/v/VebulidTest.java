package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Vebulid.class, RayOfCommand.class})
class VebulidTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter")
    void entersWithCounter() {
        Permanent vebulid = castVebulid(player1);

        assertThat(vebulid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("May put a +1/+1 counter on itself at upkeep")
    void mayPutCounterAtUpkeep() {
        Permanent vebulid = addReadyVebulid(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(vebulid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("May decline the upkeep counter")
    void mayDeclineCounterAtUpkeep() {
        Permanent vebulid = addReadyVebulid(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(vebulid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent vebulid = addReadyVebulid(player1);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(vebulid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNull();
    }

    @Test
    @DisplayName("Attacking destroys Vebulid at end of combat")
    void attackingDestroysItAtEndOfCombat() {
        Permanent vebulid = castVebulid(player1);
        vebulid.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vebulid");
        harness.assertInGraveyard(player1, "Vebulid");
    }

    @Test
    @DisplayName("Blocking schedules Vebulid for end-of-combat destruction")
    void blockingSchedulesDestruction() {
        Permanent attacker = addReadyVebulid(player1);
        attacker.setAttacking(true);
        Permanent vebulid = addReadyVebulid(player2);
        vebulid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals("Vebulid"));

        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(vebulid.getId())
                        && action.kind() == DelayedPermanentActionKind.DESTROY_AT_END_OF_COMBAT);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Vebulid");
        harness.assertInGraveyard(player2, "Vebulid");
    }

    @Test
    @DisplayName("Delayed destruction retains its controller after Vebulid changes control")
    void delayedDestructionRetainsOriginalController() {
        Permanent vebulid = addReadyVebulid(player1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            harness.ensurePriority(player2);
            harness.castAndResolveInstant(player2, 0, vebulid.getId());
            harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        });

        harness.assertOnBattlefield(player2, "Vebulid");
        assertThat(gd.stack).anySatisfy(entry -> {
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getSourcePermanentId()).isEqualTo(vebulid.getId());
            assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        });
    }

    @Test
    @DisplayName("Entering without being cast still supplies the counter before state-based actions")
    void entersWithoutCastingWithCounter() {
        Permanent vebulid = harness.enterBattlefieldAndReturn(player1, new Vebulid());
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Vebulid");
        assertThat(vebulid.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent castVebulid(Player player) {
        harness.castFromHand(player, new Vebulid(), "{B}");
        harness.passBothPriorities();
        return findPermanent(player, "Vebulid");
    }

    private Permanent addReadyVebulid(Player player) {
        Permanent vebulid = addCreatureReady(player, new Vebulid());
        vebulid.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        return vebulid;
    }
}
