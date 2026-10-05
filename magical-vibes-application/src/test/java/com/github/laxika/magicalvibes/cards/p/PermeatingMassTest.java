package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PermeatingMass.class, GiantSpider.class, HillGiant.class})
class PermeatingMassTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a creature makes it a copy of Permeating Mass")
    void combatDamageToCreatureMakesItACopy() {
        Permanent mass = addCreatureReady(player1, new PermeatingMass());
        mass.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        resolveAllTriggers();

        assertThat(blocker.getCard().getName()).isEqualTo("Permeating Mass");
        assertThat(blocker.getCard().getPower()).isEqualTo(1);
        assertThat(blocker.getCard().getToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The trigger uses Permeating Mass's last-known characteristics if it dies in combat")
    void triggerUsesLastKnownSourceIfMassDies() {
        Permanent mass = addCreatureReady(player1, new PermeatingMass());
        mass.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new HillGiant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mass);
        assertThat(blocker.getCard().getName()).isEqualTo("Permeating Mass");
    }

    @Test
    @DisplayName("Combat damage to a player does not trigger Permeating Mass")
    void combatDamageToPlayerDoesNotTrigger() {
        Permanent mass = addCreatureReady(player1, new PermeatingMass());
        mass.setAttacking(true);
        Permanent creature = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(creature.getCard().getName()).isEqualTo("Giant Spider");
    }

    @Test
    @DisplayName("Permeating Mass also copies an attacker when blocking")
    void blockingMassCopiesAttacker() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        addCreatureReady(player2, new PermeatingMass());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(attacker.getCard().getName()).isEqualTo("Permeating Mass");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    @DisplayName("Source counters are not copied and recipient counters remain")
    void copyPreservesRecipientCountersWithoutCopyingSourceCounters() {
        Permanent mass = addCreatureReady(player1, new PermeatingMass());
        mass.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        mass.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(blocker.getCard().getName()).isEqualTo("Permeating Mass");
        assertThat(blocker.getPlusOnePlusOneCounters()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Damage remains marked and can become lethal after copying")
    void reducedToughnessMakesMarkedDamageLethal() {
        Permanent mass = addCreatureReady(player1, new PermeatingMass());
        mass.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        mass.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Giant Spider"));
    }

    @Test
    @DisplayName("A copied creature can spread the copy ability in a later combat")
    void copiedCreatureSpreadsTheAbility() {
        Permanent mass = addCreatureReady(player1, new PermeatingMass());
        mass.setAttacking(true);
        Permanent copiedCreature = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        mass.setAttacking(false);
        mass.setMarkedDamage(0);
        copiedCreature.setMarkedDamage(0);
        copiedCreature.setAttacking(true);
        Permanent nextCreature = addCreatureReady(player1, new GiantSpider());

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(nextCreature.getCard().getName()).isEqualTo("Permeating Mass");
        assertThat(gqs.getEffectivePower(gd, nextCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nextCreature)).isEqualTo(3);
    }
}
