package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        blockHillGiant(findPermanent(player1, "Skeleton Scavengers"));

        Permanent regenerated = findPermanent(player1, "Skeleton Scavengers");
        assertThat(regenerated.getRegenerationShield()).isZero();
        assertThat(regenerated.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A foreign regeneration shield does not trigger the rider")
    void foreignShieldDoesNotAddCounter() {
        Permanent scavengers = addScavengersReady();
        scavengers.setRegenerationShield(1);

        blockHillGiant(scavengers);

        Permanent regenerated = findPermanent(player1, "Skeleton Scavengers");
        assertThat(regenerated.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addScavengersReady() {
        castScavengers();
        return findPermanent(player1, "Skeleton Scavengers");
    }

    private void castScavengers() {
        harness.setHand(player1, List.of(new SkeletonScavengers()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void blockHillGiant(Permanent scavengers) {
        scavengers.setBlocking(true);
        scavengers.addBlockingTarget(0);

        Permanent attacker = new Permanent(new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
