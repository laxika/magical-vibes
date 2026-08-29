package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WildfireCerberusTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming monstrous deals 2 damage to each opponent and their creatures")
    void becomingMonstrousDamagesOpponentsAndTheirCreatures() {
        Permanent cerberus = addReadyCerberus();
        Permanent ownCreature = addReadyCreature(player1, new GrizzlyBears());
        Permanent opponentCreature = addReadyCreature(player2, new AirElemental());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(cerberus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(cerberus.isMonstrous()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(ownCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Wildfire Cerberus's monstrosity ability can resolve only once")
    void monstrosityOnlyResolvesOnce() {
        addReadyCerberus();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        addMonstrosityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already monstrous");
    }

    private Permanent addReadyCerberus() {
        Permanent cerberus = harness.addToBattlefieldAndReturn(player1, new WildfireCerberus());
        cerberus.setSummoningSick(false);
        return cerberus;
    }

    private Permanent addReadyCreature(Player player, Card creature) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, creature);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
