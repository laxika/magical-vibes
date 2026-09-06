package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeeryFogbeast.class, GrizzlyBears.class})
class LeeryFogbeastTest extends BaseCardTest {

    @Test
    @DisplayName("When Leery Fogbeast becomes blocked, all combat damage is prevented")
    void becomingBlockedPreventsAllCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent fogbeast = addAttackingCreature(player1, player2, new LeeryFogbeast());
        addAttackingCreature(player1, player2, new GrizzlyBears());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

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
        addAttackingCreature(player1, player2, new LeeryFogbeast());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    private Permanent addAttackingCreature(Player attacker, Player defender, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(attacker, card);
        permanent.setAttacking(true);
        permanent.setAttackTarget(defender.getId());
        return permanent;
    }
}
