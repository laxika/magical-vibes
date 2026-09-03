package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CloudElemental;
import com.github.laxika.magicalvibes.cards.p.Python;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LongbowArcher.class, CloudElemental.class, Python.class})
class LongbowArcherTest extends BaseCardTest {

    @Test
    @DisplayName("Reach lets Longbow Archer block a creature with flying")
    void reachCanBlockFlyer() {
        Permanent flyer = addCreatureReady(player1, new CloudElemental());
        flyer.setAttacking(true);
        Permanent archer = addCreatureReady(player2, new LongbowArcher());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, archer), indexOf(player1, flyer))));

        assertThat(archer.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature without flying or reach cannot block the flyer")
    void nonReachCannotBlockFlyer() {
        Permanent flyer = addCreatureReady(player1, new CloudElemental());
        flyer.setAttacking(true);
        Permanent python = addCreatureReady(player2, new Python());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, python), indexOf(player1, flyer)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("First strike defeats a 3/2 blocker before it deals combat damage")
    void firstStrikeDealsCombatDamageFirst() {
        Permanent archer = addCreatureReady(player1, new LongbowArcher());
        archer.setAttacking(true);

        Permanent python = addCreatureReady(player2, new Python());
        python.setBlocking(true);
        python.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Longbow Archer");
        harness.assertInGraveyard(player2, "Python");
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
