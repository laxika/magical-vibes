package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KjeldoranJavelineer.class, KjeldoranOutrider.class})
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
        assertThat(javelineer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deals no damage when it has no age counters")
    void dealsNoDamageWithoutAgeCounters() {
        Permanent javelineer = addReadyJavelineer();
        Permanent attacker = addCombatCreature(player2, true, false);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(javelineer.isTapped()).isTrue();
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
    @DisplayName("Does not deal damage if the target stops attacking before resolution")
    void targetMustStillBeAttackingAtResolution() {
        Permanent javelineer = addReadyJavelineer();
        javelineer.setCounterCount(CounterType.AGE, 1);
        Permanent attacker = addCombatCreature(player2, true, false);

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
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

    @Test
    @DisplayName("Damage uses the age counter count at resolution")
    void usesCurrentAgeCounters() {
        Permanent javelineer = addReadyJavelineer();
        javelineer.setCounterCount(CounterType.AGE, 2);
        Permanent attacker = addCombatCreature(player2, true, false);

        harness.activateAbility(player1, 0, null, attacker.getId());
        javelineer.setCounterCount(CounterType.AGE, 1);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
    }

    @Test
    @DisplayName("Damage uses the final age counter count when the source leaves the battlefield")
    void usesLastKnownAgeCounters() {
        Permanent javelineer = addReadyJavelineer();
        javelineer.setCounterCount(CounterType.AGE, 1);
        Permanent attacker = addCombatCreature(player2, true, false);

        harness.activateAbility(player1, 0, null, attacker.getId());
        javelineer.setCounterCount(CounterType.AGE, 2);
        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, javelineer);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kjeldoran Javelineer");
        harness.assertInGraveyard(player2, "Kjeldoran Outrider");
    }

    @Test
    @DisplayName("The second cumulative upkeep requires two mana")
    void paysForEveryAgeCounter() {
        Permanent javelineer = harness.addToBattlefieldAndReturn(player1, new KjeldoranJavelineer());
        javelineer.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(javelineer.getCounterCount(CounterType.AGE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(javelineer);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Cannot pay only part of cumulative upkeep")
    void insufficientManaSacrifices() {
        Permanent javelineer = harness.addToBattlefieldAndReturn(player1, new KjeldoranJavelineer());
        javelineer.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(javelineer);
        harness.assertInGraveyard(player1, "Kjeldoran Javelineer");
    }

    @Test
    @DisplayName("Cumulative upkeep does not trigger during the opponent's upkeep")
    void opponentUpkeepDoesNotAddAgeCounter() {
        Permanent javelineer = harness.addToBattlefieldAndReturn(player1, new KjeldoranJavelineer());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(javelineer.getCounterCount(CounterType.AGE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(javelineer);
    }

    private Permanent addReadyJavelineer() {
        return addCreatureReady(player1, new KjeldoranJavelineer());
    }

    private Permanent addCombatCreature(Player player, boolean attacking, boolean blocking) {
        Permanent creature = addCreatureReady(player, new KjeldoranOutrider());
        creature.setAttacking(attacking);
        creature.setBlocking(blocking);
        if (attacking) {
            creature.setAttackTarget(player1.getId());
        }
        return creature;
    }
}
