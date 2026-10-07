package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.c.CoordinatedAssault;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StaunchHeartedWarrior.class, Shock.class, GiantGrowth.class, CoordinatedAssault.class})
class StaunchHeartedWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Staunch-Hearted Warrior puts two +1/+1 counters on it")
    void castingSpellThatTargetsWarriorTriggersHeroic() {
        harness.addToBattlefield(player1, new StaunchHeartedWarrior());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID warriorId = harness.getPermanentId(player1, "Staunch-Hearted Warrior");
        harness.castAndResolveInstant(player1, 0, warriorId);
        harness.passBothPriorities();

        Permanent warrior = findPermanent(player1, "Staunch-Hearted Warrior");
        assertThat(warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(warrior.getEffectivePower()).isEqualTo(4);
        assertThat(warrior.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger Staunch-Hearted Warrior")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new StaunchHeartedWarrior());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        Permanent warrior = findPermanent(player1, "Staunch-Hearted Warrior");
        assertThat(warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's spell that targets Staunch-Hearted Warrior does not trigger it")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new StaunchHeartedWarrior());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID warriorId = harness.getPermanentId(player1, "Staunch-Hearted Warrior");
        harness.castAndResolveInstant(player2, 0, warriorId);

        Permanent warrior = findPermanent(player1, "Staunch-Hearted Warrior");
        assertThat(warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void multiTargetSpellTriggersEachTargetedWarriorOnceBeforeSpellResolves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new StaunchHeartedWarrior());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new StaunchHeartedWarrior());
        harness.setHand(player1, List.of(new CoordinatedAssault()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(first.getPowerModifier()).isZero();
        assertThat(second.getPowerModifier()).isZero();

        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(second.getPowerModifier()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void targetingAnotherWarriorDoesNotTriggerUntargetedWarrior() {
        Permanent targeted = harness.addToBattlefieldAndReturn(player1, new StaunchHeartedWarrior());
        Permanent untargeted = harness.addToBattlefieldAndReturn(player1, new StaunchHeartedWarrior());
        harness.setHand(player1, List.of(new CoordinatedAssault()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.of(targeted.getId()));
        harness.passBothPriorities();

        assertThat(targeted.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(untargeted.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void spellWithNoChosenTargetsDoesNotTriggerHeroic() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new StaunchHeartedWarrior());
        harness.setHand(player1, List.of(new CoordinatedAssault()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.<UUID>of());

        assertThat(warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Coordinated Assault");
    }

    @Test
    void successiveTargetingSpellsEachAddTwoCounters() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new StaunchHeartedWarrior());
        harness.setHand(player1, List.of(new CoordinatedAssault(), new CoordinatedAssault()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, warrior.getId());
        harness.passBothPriorities();
        assertThat(warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.castAndResolveInstant(player1, 0, warrior.getId());
        harness.passBothPriorities();

        assertThat(warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }
}
