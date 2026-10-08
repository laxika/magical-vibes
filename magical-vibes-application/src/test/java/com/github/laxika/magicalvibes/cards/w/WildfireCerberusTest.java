package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildfireCerberus.class, GrizzlyBears.class, AirElemental.class})
class WildfireCerberusTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming monstrous deals 2 damage to each opponent and their creatures")
    void becomingMonstrousDamagesOpponentsAndTheirCreatures() {
        Permanent cerberus = addReadyCerberus();
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new AirElemental());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(cerberus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(cerberus.isMonstrous()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(ownCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Monstrosity can be activated again but does nothing when already monstrous")
    void repeatedMonstrosityActivationDoesNothing() {
        Permanent cerberus = addReadyCerberus();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(cerberus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(cerberus.isMonstrous()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Two pending monstrosity activations produce only one counter and one damage trigger")
    void multiplePendingActivationsBecomeMonstrousOnce() {
        Permanent cerberus = addReadyCerberus();
        addMonstrosityMana();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(cerberus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(cerberus.isMonstrous()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("No monstrous trigger occurs if Cerberus leaves before monstrosity resolves")
    void leavingBeforeMonstrosityResolvesDoesNotDealDamage() {
        Permanent cerberus = addReadyCerberus();
        addMonstrosityMana();
        harness.activateAbility(player1, 0, null, null);

        cerberus.setMarkedDamage(4);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cerberus);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The monstrous damage trigger resolves even after Cerberus dies")
    void leavingAfterBecomingMonstrousDoesNotStopDamageTrigger() {
        Permanent cerberus = addReadyCerberus();
        Permanent opponentCreature = addCreatureReady(player2, new AirElemental());
        addMonstrosityMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(cerberus.isMonstrous()).isTrue();
        cerberus.setMarkedDamage(5);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cerberus);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Monstrous damage kills every opposing creature with two toughness")
    void damageKillsAllOpposingSmallCreaturesAndSparesController() {
        addReadyCerberus();
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(first, second);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(first.getCard(), second.getCard());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private Permanent addReadyCerberus() {
        return addCreatureReady(player1, new WildfireCerberus());
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
