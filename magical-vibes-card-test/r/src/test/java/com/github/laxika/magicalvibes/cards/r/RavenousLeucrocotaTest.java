package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RavenousLeucrocota.class})
class RavenousLeucrocotaTest extends BaseCardTest {

    @Test
    @DisplayName("Monstrosity puts three +1/+1 counters on Ravenous Leucrocota")
    void monstrosityAddsCountersAndMarksItMonstrous() {
        Permanent leucrocota = addReadyLeucrocota();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(leucrocota.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(leucrocota.isMonstrous()).isTrue();
        assertThat(leucrocota.getEffectivePower()).isEqualTo(5);
        assertThat(leucrocota.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Monstrosity can be activated again but does nothing when already monstrous")
    void monstrosityCanBeActivatedAfterBecomingMonstrous() {
        Permanent leucrocota = addReadyLeucrocota();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(leucrocota.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(leucrocota.isMonstrous()).isTrue();
    }

    @Test
    @DisplayName("Two pending monstrosity activations add counters only once")
    void pendingMonstrosityActivationsOnlyAddCountersOnce() {
        Permanent leucrocota = addReadyLeucrocota();
        harness.addMana(player1, ManaColor.COLORLESS, 12);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(leucrocota.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(leucrocota.isMonstrous()).isTrue();
    }

    @Test
    @DisplayName("A tapped summoning-sick creature can activate monstrosity")
    void monstrosityDoesNotRequireTappingOrHaste() {
        Permanent leucrocota = harness.addToBattlefieldAndReturn(player1, new RavenousLeucrocota());
        leucrocota.setSummoningSick(true);
        leucrocota.tap();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(leucrocota.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(leucrocota.isMonstrous()).isTrue();
        assertThat(leucrocota.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Vigilance lets Ravenous Leucrocota attack without tapping")
    void vigilanceDoesNotTapAttacker() {
        Permanent leucrocota = addReadyLeucrocota();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(leucrocota.isAttacking()).isTrue();
        assertThat(leucrocota.isTapped()).isFalse();
    }

    private Permanent addReadyLeucrocota() {
        return addCreatureReady(player1, new RavenousLeucrocota());
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
