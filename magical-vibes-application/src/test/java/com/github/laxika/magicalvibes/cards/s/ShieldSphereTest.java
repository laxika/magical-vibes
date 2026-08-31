package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AgentOfStromgald;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShieldSphere.class, AgentOfStromgald.class})
class ShieldSphereTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the blocking trigger puts a -0/-1 counter on it")
    void blockingPutsMinusZeroMinusOneCounter() {
        addCreatureReady(player1, new AgentOfStromgald());
        Permanent sphere = addCreatureReady(player2, new ShieldSphere());

        declareAttackers(List.of(0));
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

        declareAttackers(List.of(0));
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

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, sphere));
        harness.passBothPriorities();

        assertThat(sphere.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isZero();
    }

    private void block() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
    }
}
