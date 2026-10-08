package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormbloodBerserker.class, Shock.class})
class StormbloodBerserkerTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 2: enters with two +1/+1 counters when an opponent was dealt damage")
    void bloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castBerserker();

        assertThat(findPermanent(player1, "Stormblood Berserker")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bloodthirst 2: enters without counters when no opponent was dealt damage")
    void bloodthirstDoesNotApply() {
        castBerserker();

        assertThat(findPermanent(player1, "Stormblood Berserker")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst 2 ignores damage dealt to its own controller")
    void bloodthirstIgnoresControllerDamage() {
        gd.recordDamageToPlayer(player1.getId(), 3);
        castBerserker();

        assertThat(findPermanent(player1, "Stormblood Berserker")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Damage dealt in response enables bloodthirst on resolution")
    void damageInResponseEnablesBloodthirst() {
        harness.castFromHand(player1, new StormbloodBerserker(), "{1}{R}");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 18);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Stormblood Berserker")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Life loss without damage does not enable bloodthirst")
    void lifeLossDoesNotEnableBloodthirst() {
        harness.setLife(player2, 17);
        castBerserker();

        assertThat(findPermanent(player1, "Stormblood Berserker")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst applies immediately to entry without casting and is fixed at two counters")
    void bloodthirstAppliesToNoncastEntry() {
        gd.recordDamageToPlayer(player2.getId(), 7);

        var permanent = harness.enterBattlefieldAndReturn(player1, new StormbloodBerserker());

        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bloodthirst uses the entering creature's controller to identify opponents")
    void bloodthirstUsesEnteringController() {
        gd.recordDamageToPlayer(player1.getId(), 1);

        var permanent = harness.enterBattlefieldAndReturn(player2, new StormbloodBerserker());

        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Menace rejects a single blocker")
    void menaceRejectsSingleBlocker() {
        addCreatureReady(player1, new StormbloodBerserker());
        addCreatureReady(player2, new StormbloodBerserker());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");
    }

    @Test
    @DisplayName("Menace allows two blockers")
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new StormbloodBerserker());
        var firstBlocker = addCreatureReady(player2, new StormbloodBerserker());
        var secondBlocker = addCreatureReady(player2, new StormbloodBerserker());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Menace allows the creature to remain unblocked")
    void menaceAllowsNoBlockers() {
        addCreatureReady(player1, new StormbloodBerserker());
        addCreatureReady(player2, new StormbloodBerserker());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 19);
    }

    private void castBerserker() {
        harness.castFromHand(player1, new StormbloodBerserker(), "{1}{R}");
        resolveAllTriggers();
    }
}
