package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StaunchHeartedWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Staunch-Hearted Warrior puts two +1/+1 counters on it")
    void castingSpellThatTargetsWarriorTriggersHeroic() {
        harness.addToBattlefield(player1, new StaunchHeartedWarrior());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID warriorId = harness.getPermanentId(player1, "Staunch-Hearted Warrior");
        harness.castInstant(player1, 0, warriorId);
        harness.passBothPriorities();
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

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

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
        harness.castInstant(player2, 0, warriorId);
        harness.passBothPriorities();

        Permanent warrior = findPermanent(player1, "Staunch-Hearted Warrior");
        assertThat(warrior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
