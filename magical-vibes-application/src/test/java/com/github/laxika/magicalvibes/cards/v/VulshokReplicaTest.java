package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.k.KothOfTheHammer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VulshokReplica.class, KothOfTheHammer.class})
class VulshokReplicaTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Vulshok Replica deals 3 damage to target player")
    void dealsDamageToPlayer() {
        harness.addToBattlefield(player1, new VulshokReplica());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Vulshok Replica is sacrificed as part of the cost")
    void sacrificedAsCost() {
        harness.addToBattlefield(player1, new VulshokReplica());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Vulshok Replica");
        harness.assertInGraveyard(player1, "Vulshok Replica");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Cannot target a creature with the ability")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new VulshokReplica());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VulshokReplica());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player or planeswalker");
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new VulshokReplica());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target self with the ability")
    void canTargetSelf() {
        harness.addToBattlefield(player1, new VulshokReplica());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }


    @Test
    @DisplayName("Damage removes loyalty from a planeswalker without damaging its controller")
    void dealsDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new VulshokReplica());
        Permanent koth = harness.addToBattlefieldAndReturn(player2, new KothOfTheHammer());
        koth.setCounterCount(CounterType.LOYALTY, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, koth.getId());
        harness.passBothPriorities();

        assertThat(koth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Koth of the Hammer");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A planeswalker with three loyalty dies from the damage")
    void lethalDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new VulshokReplica());
        Permanent koth = harness.addToBattlefieldAndReturn(player2, new KothOfTheHammer());
        koth.setCounterCount(CounterType.LOYALTY, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, koth.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Koth of the Hammer");
        harness.assertInGraveyard(player2, "Koth of the Hammer");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A tapped creature with summoning sickness can activate the sacrifice ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent replica = harness.addToBattlefieldAndReturn(player1, new VulshokReplica());
        replica.tap();
        replica.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Vulshok Replica");
    }

}
