package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeeryFogbeast.class, ElvishWarrior.class, Shock.class})
class LeeryFogbeastTest extends BaseCardTest {

    @Test
    @DisplayName("When Leery Fogbeast becomes blocked, all combat damage is prevented")
    void becomingBlockedPreventsAllCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent fogbeast = addCreatureReady(player1, new LeeryFogbeast());
        addCreatureReady(player1, new ElvishWarrior());
        Permanent blocker = addCreatureReady(player2, new ElvishWarrior());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(fogbeast))));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(fogbeast.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An unblocked Leery Fogbeast does not prevent combat damage")
    void unblockedDoesNotPreventCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new LeeryFogbeast());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("The triggered prevention does not prevent noncombat damage")
    void doesNotPreventNoncombatDamage() {
        harness.setLife(player2, 20);
        Permanent fogbeast = addCreatureReady(player1, new LeeryFogbeast());
        Permanent blocker = addCreatureReady(player2, new ElvishWarrior());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(fogbeast))));
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Leery Fogbeast blocking does not prevent combat damage")
    void blockingDoesNotTriggerPrevention() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new ElvishWarrior());
        addCreatureReady(player1, new ElvishWarrior());
        Permanent fogbeast = addCreatureReady(player2, new LeeryFogbeast());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(fogbeast);
    }

    @Test
    @DisplayName("Removing Leery Fogbeast in response does not stop its prevention trigger")
    void preventionResolvesAfterSourceDies() {
        harness.setLife(player2, 20);
        Permanent fogbeast = addCreatureReady(player1, new LeeryFogbeast());
        addCreatureReady(player1, new ElvishWarrior());
        Permanent blocker = addCreatureReady(player2, new ElvishWarrior());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player1, 0, fogbeast.getId());
        resolveAllTriggers();
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fogbeast);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(blocker.getMarkedDamage()).isZero();
    }
}
