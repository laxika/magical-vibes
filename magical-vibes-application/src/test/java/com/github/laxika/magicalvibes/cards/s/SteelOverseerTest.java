package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CrystalBall;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({SteelOverseer.class, Ornithopter.class, GrizzlyBears.class, CrystalBall.class})
class SteelOverseerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability puts a +1/+1 counter on each artifact creature you control")
    void putsCountersOnArtifactCreatures() {
        Permanent overseer = addReadyOverseer(player1);
        Permanent ornithopter = addReadyArtifactCreature(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        // Both Steel Overseer (artifact creature) and Ornithopter (artifact creature) get counters
        assertThat(overseer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ornithopter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not put counters on non-artifact creatures")
    void doesNotAffectNonArtifactCreatures() {
        addReadyOverseer(player1);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not put counters on opponent's artifact creatures")
    void doesNotAffectOpponentArtifactCreatures() {
        addReadyOverseer(player1);
        Permanent opponentArtifact = addReadyArtifactCreature(player2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(opponentArtifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Activating ability taps Steel Overseer")
    void activatingTapsOverseer() {
        Permanent overseer = addReadyOverseer(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(overseer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent overseer = harness.addToBattlefieldAndReturn(player1, new SteelOverseer());
        overseer.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters accumulate across multiple activations")
    void countersAccumulate() {
        Permanent overseer = addReadyOverseer(player1);
        Permanent ornithopter = addReadyArtifactCreature(player1);

        // First activation
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ornithopter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        // Untap overseer for second activation
        overseer.untap();

        // Second activation
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ornithopter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void putsAbilityOnStack() {
        Permanent overseer = addReadyOverseer(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(overseer.getId());
    }

    private Permanent addReadyOverseer(Player player) {
        return addCreatureReady(player, new SteelOverseer());
    }

    private Permanent addReadyArtifactCreature(Player player) {
        return addCreatureReady(player, new Ornithopter());
    }

    @Test
    @DisplayName("Noncreature artifacts do not receive counters")
    void doesNotAffectNoncreatureArtifacts() {
        Permanent overseer = addReadyOverseer(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new CrystalBall());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(overseer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Artifact creatures entering before resolution receive counters even when tapped and summoning sick")
    void includesArtifactCreaturesEnteringBeforeResolution() {
        addReadyOverseer(player1);
        harness.activateAbility(player1, 0, null, null);
        Permanent ornithopter = harness.enterBattlefieldAndReturn(player1, new Ornithopter());
        ornithopter.setSummoningSick(true);
        ornithopter.tap();

        harness.passBothPriorities();

        assertThat(ornithopter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate while already tapped")
    void cannotActivateWhileTapped() {
        Permanent overseer = addReadyOverseer(player1);
        overseer.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(overseer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
