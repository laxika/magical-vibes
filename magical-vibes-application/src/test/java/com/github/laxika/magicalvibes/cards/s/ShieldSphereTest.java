package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AgentOfStromgald;
import com.github.laxika.magicalvibes.cards.v.ValorMadeReal;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShieldSphere.class, AgentOfStromgald.class, ValorMadeReal.class})
class ShieldSphereTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the blocking trigger puts a -0/-1 counter on it")
    void blockingPutsMinusZeroMinusOneCounter() {
        addCreatureReady(player1, new AgentOfStromgald());
        Permanent sphere = addCreatureReady(player2, new ShieldSphere());

        declareAttackersAndPrepareBlockers(List.of(0));
        block();

        assertThat(sphere.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, sphere)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, sphere)).isEqualTo(5);
    }

    @Test
    @DisplayName("Blocking preserves an existing -0/-1 counter and adds another")
    void blockingAddsToExistingMinusZeroMinusOneCounter() {
        addCreatureReady(player1, new AgentOfStromgald());
        Permanent sphere = addCreatureReady(player2, new ShieldSphere());
        sphere.setCounterCount(CounterType.MINUS_ZERO_MINUS_ONE, 1);

        declareAttackersAndPrepareBlockers(List.of(0));
        block();

        assertThat(sphere.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sphere)).isEqualTo(4);
    }

    @Test
    @DisplayName("Sitting on the battlefield without blocking gives no counter")
    void noCounterWithoutBlocking() {
        Permanent sphere = addCreatureReady(player2, new ShieldSphere());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(sphere.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The blocking trigger does nothing if Shield Sphere leaves before resolution")
    void triggerDoesNothingIfSphereLeavesBeforeResolution() {
        addCreatureReady(player1, new AgentOfStromgald());
        Permanent sphere = addCreatureReady(player2, new ShieldSphere());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, sphere));
        harness.passBothPriorities();

        assertThat(sphere.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Blocking multiple creatures puts only one counter on Shield Sphere")
    void blockingMultipleCreaturesTriggersOnlyOnce() {
        addCreatureReady(player1, new AgentOfStromgald());
        addCreatureReady(player1, new AgentOfStromgald());
        Permanent sphere = addCreatureReady(player2, new ShieldSphere());
        harness.setHand(player2, List.of(new ValorMadeReal()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player2, 0, sphere.getId());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));
        resolveAllTriggers();

        assertThat(sphere.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sphere)).isEqualTo(5);
    }

    @Test
    @DisplayName("The sixth counter sends Shield Sphere to the graveyard before combat damage")
    void sixthCounterCausesZeroToughnessBeforeCombatDamage() {
        addCreatureReady(player1, new AgentOfStromgald());
        Permanent sphere = addCreatureReady(player2, new ShieldSphere());
        sphere.setCounterCount(CounterType.MINUS_ZERO_MINUS_ONE, 5);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(sphere.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isEqualTo(5);
        harness.assertOnBattlefield(player2, "Shield Sphere");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Shield Sphere");
        harness.assertInGraveyard(player2, "Shield Sphere");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void block() {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
    }
}
