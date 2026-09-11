package com.github.laxika.magicalvibes.cards.s;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkeletonScavengers.class, SpinedWurm.class})
class SkeletonScavengersTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter")
    void entersWithCounter() {
        castScavengers();

        Permanent scavengers = findPermanent(player1, "Skeleton Scavengers");
        assertThat(scavengers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration costs one mana per +1/+1 counter")
    void regenerationCostScalesWithCounters() {
        Permanent scavengers = addScavengersReady();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(scavengers.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate regeneration without paying for each counter")
    void cannotActivateWithoutEnoughMana() {
        addScavengersReady();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The regeneration rider adds a +1/+1 counter only when its shield is spent")
    void regenerationAddsCounterWhenShieldIsSpent() {
        Permanent scavengers = addScavengersReady();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(scavengers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        blockWithSpinedWurm(findPermanent(player1, "Skeleton Scavengers"));

        Permanent regenerated = findPermanent(player1, "Skeleton Scavengers");
        assertThat(regenerated.getRegenerationShield()).isZero();
        assertThat(regenerated.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Regeneration costs more after its rider adds a counter")
    void regenerationCostIncreasesWithRiderCounter() {
        Permanent scavengers = addScavengersReady();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        blockWithSpinedWurm(scavengers);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(scavengers.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The counter rider waits for regeneration to occur")
    void counterRiderWaitsForRegeneration() {
        Permanent scavengers = addScavengersReady();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        scavengers.setMarkedDamage(1);
        harness.runStateBasedActions();

        assertThat(scavengers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(scavengers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A foreign regeneration shield does not trigger the rider")
    void foreignShieldDoesNotAddCounter() {
        Permanent scavengers = addScavengersReady();
        scavengers.setRegenerationShield(1);

        blockWithSpinedWurm(scavengers);

        Permanent regenerated = findPermanent(player1, "Skeleton Scavengers");
        assertThat(regenerated.getRegenerationShield()).isZero();
        assertThat(regenerated.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addScavengersReady() {
        castScavengers();
        return findPermanent(player1, "Skeleton Scavengers");
    }

    private void castScavengers() {
        harness.castFromHand(player1, new SkeletonScavengers(), "{2}{B}");
        harness.passBothPriorities();
    }

    private void blockWithSpinedWurm(Permanent scavengers) {
        Permanent attacker = addCreatureReady(player2, new SpinedWurm());
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scavengers);

        declareAttackers(player2, List.of(attackerIndex));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
