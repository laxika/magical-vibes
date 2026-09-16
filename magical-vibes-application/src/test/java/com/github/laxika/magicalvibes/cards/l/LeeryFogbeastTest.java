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

        declareAttackers(List.of(0, 1));
        prepareDeclareBlockers();
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

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
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

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(fogbeast))));
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }
}
