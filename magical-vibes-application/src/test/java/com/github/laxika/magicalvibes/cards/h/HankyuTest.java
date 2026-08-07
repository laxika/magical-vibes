package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HankyuTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip attaches Hankyu to target creature")
    void equipAttachesToCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent hankyu = addHankyuReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(hankyu.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Granted aim-counter ability puts the counter on Hankyu, not on the equipped creature")
    void aimCounterGoesOnHankyu() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent hankyu = addHankyuReady(player1);
        hankyu.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(hankyu.getCounterCount(CounterType.AIM)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.AIM)).isZero();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing three aim counters deals 3 damage to a player")
    void removingAimCountersDealsDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent hankyu = addHankyuReady(player1);
        hankyu.setAttachedTo(creature.getId());
        hankyu.setCounterCount(CounterType.AIM, 3);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(hankyu.getCounterCount(CounterType.AIM)).isZero();
    }

    @Test
    @DisplayName("Damage equal to the aim counters removed kills a creature")
    void removingAimCountersKillsCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent hankyu = addHankyuReady(player1);
        hankyu.setAttachedTo(creature.getId());
        hankyu.setCounterCount(CounterType.AIM, 2);

        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, victim.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(victim.getId()));
    }

    @Test
    @DisplayName("With no aim counters the ability deals no damage")
    void noAimCountersDealsNoDamage() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent hankyu = addHankyuReady(player1);
        hankyu.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The equipped creature is the damage source, not Hankyu")
    void damageSourceIsEquippedCreature() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent hankyu = addHankyuReady(player1);
        hankyu.setAttachedTo(creature.getId());
        hankyu.setCounterCount(CounterType.AIM, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("damage from Grizzly Bears"));
    }

    @Test
    @DisplayName("Aim counters stay on Hankyu when it is unattached, and are removed by a later activation")
    void aimCountersPersistAcrossUnattach() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent hankyu = addHankyuReady(player1);
        hankyu.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        hankyu.setAttachedTo(null);
        assertThat(hankyu.getCounterCount(CounterType.AIM)).isEqualTo(1);

        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        hankyu.setAttachedTo(other.getId());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 2, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(hankyu.getCounterCount(CounterType.AIM)).isZero();
    }

    private Permanent addHankyuReady(Player player) {
        Permanent perm = new Permanent(new Hankyu());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
