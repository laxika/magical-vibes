package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.j.JiwariTheEarthAflame;
import com.github.laxika.magicalvibes.cards.k.KitsuneLoreweaver;
import com.github.laxika.magicalvibes.cards.m.MinamoScrollkeeper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuneTailKitsuneAscendant.class, RuneTailsEssence.class,
        JiwariTheEarthAflame.class, KitsuneLoreweaver.class, MinamoScrollkeeper.class})
class RuneTailKitsuneAscendantTest extends BaseCardTest {

    @Test
    @DisplayName("Flips when its controller has 30 or more life")
    void flipsAtThirtyLife() {
        Permanent runeTail = harness.addToBattlefieldAndReturn(player1, new RuneTailKitsuneAscendant());
        harness.setLife(player1, 30);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(runeTail.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not flip below 30 life")
    void doesNotFlipBelowThirtyLife() {
        Permanent runeTail = harness.addToBattlefieldAndReturn(player1, new RuneTailKitsuneAscendant());
        harness.setLife(player1, 29);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(runeTail.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Still flips if life falls below 30 before the trigger resolves")
    void stillFlipsIfLifeDropsBeforeTriggerResolves() {
        Permanent runeTail = harness.addToBattlefieldAndReturn(player1, new RuneTailKitsuneAscendant());
        harness.setLife(player1, 30);
        harness.runStateBasedActions();

        harness.setLife(player1, 29);
        harness.passBothPriorities();

        assertThat(runeTail.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not flip when only an opponent has 30 or more life")
    void doesNotFlipForOpponentsLife() {
        Permanent runeTail = harness.addToBattlefieldAndReturn(player1, new RuneTailKitsuneAscendant());
        harness.setLife(player2, 30);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(runeTail.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Rune-Tail's Essence prevents noncombat damage to creatures its controller controls")
    void essencePreventsNoncombatDamageToControlledCreatures() {
        transformRuneTail();
        Permanent creature = addCreatureReady(player1, new MinamoScrollkeeper());
        Permanent jiwari = addCreatureReady(player2, new JiwariTheEarthAflame());

        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(jiwari), 2, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Rune-Tail's Essence prevents combat damage to creatures its controller controls")
    void essencePreventsCombatDamageToControlledCreatures() {
        transformRuneTail();
        Permanent blocker = addCreatureReady(player1, new MinamoScrollkeeper());
        Permanent attacker = addCreatureReady(player2, new KitsuneLoreweaver());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player2.getId()).indexOf(attacker))));
        resolveCombat(player2);

        assertThat(blocker.getMarkedDamage()).isZero();
    }

    private Permanent transformRuneTail() {
        Permanent runeTail = harness.addToBattlefieldAndReturn(player1, new RuneTailKitsuneAscendant());
        harness.setLife(player1, 30);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        return runeTail;
    }
}
