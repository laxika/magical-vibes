package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CatWarriors;
import com.github.laxika.magicalvibes.cards.d.DevouringDeep;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GostaDirk.class, DevouringDeep.class, CatWarriors.class, Island.class, Forest.class})
class GostaDirkTest extends BaseCardTest {

    @Test
    @DisplayName("Islandwalk can be blocked while Gosta Dirk is on the battlefield")
    void islandwalkCanBeBlocked() {
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player1, new GostaDirk());
        Permanent attacker = addCreatureReady(player1, new DevouringDeep());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CatWarriors());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Gosta Dirk does not affect other landwalk abilities")
    void otherLandwalkRemainsUnblockable() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GostaDirk());
        Permanent attacker = addCreatureReady(player1, new CatWarriors());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DevouringDeep());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped Gosta Dirk controlled by the defender also allows islandwalk to be blocked")
    void defendingPlayersTappedGostaAllowsBlocking() {
        harness.addToBattlefield(player2, new Island());
        Permanent gosta = harness.addToBattlefieldAndReturn(player2, new GostaDirk());
        gosta.tap();
        Permanent attacker = addCreatureReady(player1, new DevouringDeep());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CatWarriors());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Islandwalk prevents blocking again after Gosta Dirk leaves the battlefield")
    void islandwalkReturnsAfterGostaLeaves() {
        harness.addToBattlefield(player2, new Island());
        Permanent gosta = harness.addToBattlefieldAndReturn(player1, new GostaDirk());
        Permanent attacker = addCreatureReady(player1, new DevouringDeep());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CatWarriors());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, gosta));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gosta Dirk kills its blocker before the blocker deals combat damage")
    void firstStrikeKillsBlockerBeforeItDealsDamage() {
        Permanent attacker = addCreatureReady(player1, new GostaDirk());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CatWarriors());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);
        resolveCombat();

        harness.assertInGraveyard(player2, "Cat Warriors");
        harness.assertOnBattlefield(player1, "Gosta Dirk");
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
