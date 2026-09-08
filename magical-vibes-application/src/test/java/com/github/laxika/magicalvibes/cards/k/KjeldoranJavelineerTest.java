package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KjeldoranJavelineerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to its age counters to an attacking creature")
    void dealsDamageEqualToAgeCountersToAttacker() {
        Permanent javelineer = addReadyJavelineer();
        javelineer.setCounterCount(CounterType.AGE, 2);
        Permanent attacker = addCombatCreature(player2, true, false);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target a blocking creature")
    void dealsDamageToBlocker() {
        Permanent javelineer = addReadyJavelineer();
        javelineer.setCounterCount(CounterType.AGE, 1);
        Permanent blocker = addCombatCreature(player2, false, true);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature that is neither attacking nor blocking")
    void cannotTargetIdleCreature() {
        addReadyJavelineer();
        Permanent idle = addCombatCreature(player2, false, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, idle.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps the Javelineer and adds an age counter")
    void paysCumulativeUpkeep() {
        Permanent javelineer = harness.addToBattlefieldAndReturn(player1, new KjeldoranJavelineer());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(javelineer.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(javelineer);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices the Javelineer")
    void declineSacrifices() {
        Permanent javelineer = harness.addToBattlefieldAndReturn(player1, new KjeldoranJavelineer());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(javelineer);
        harness.assertInGraveyard(player1, "Kjeldoran Javelineer");
    }

    private Permanent addReadyJavelineer() {
        return addCreatureReady(player1, new KjeldoranJavelineer());
    }

    private Permanent addCombatCreature(Player player, boolean attacking, boolean blocking) {
        Permanent creature = addCreatureReady(player, new HillGiant());
        creature.setAttacking(attacking);
        creature.setBlocking(blocking);
        if (attacking) {
            creature.setAttackTarget(player1.getId());
        }
        return creature;
    }
}
