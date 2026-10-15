package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GodlessShrine;
import com.github.laxika.magicalvibes.cards.w.WildCantor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DryadSophisticate.class, GodlessShrine.class, Forest.class, WildCantor.class})
class DryadSophisticateTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be blocked while defending player controls a nonbasic land")
    void cannotBeBlockedWithNonbasicLand() {
        harness.addToBattlefield(player2, new GodlessShrine());
        Permanent blocker = addCreatureReady(player2, new WildCantor());
        Permanent attacker = addCreatureReady(player1, new DryadSophisticate());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Can be blocked when defending player controls only basic lands")
    void canBeBlockedWithBasicLand() {
        harness.addToBattlefield(player2, new Forest());
        Permanent blocker = addCreatureReady(player2, new WildCantor());
        Permanent attacker = addCreatureReady(player1, new DryadSophisticate());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can be blocked when defending player controls no lands")
    void canBeBlockedWithoutLand() {
        Permanent blocker = addCreatureReady(player2, new WildCantor());
        Permanent attacker = addCreatureReady(player1, new DryadSophisticate());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Attacking player's nonbasic land does not prevent blocking")
    void canBeBlockedWhenOnlyAttackerControlsNonbasicLand() {
        harness.addToBattlefield(player1, new GodlessShrine());
        Permanent blocker = addCreatureReady(player2, new WildCantor());
        Permanent attacker = addCreatureReady(player1, new DryadSophisticate());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can be blocked after defending player's last nonbasic land leaves")
    void canBeBlockedAfterNonbasicLandLeaves() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new GodlessShrine());
        Permanent blocker = addCreatureReady(player2, new WildCantor());
        Permanent attacker = addCreatureReady(player1, new DryadSophisticate());
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).remove(land);
        gd.playerGraveyards.get(player2.getId()).add(land.getCard());

        prepareDeclareBlockers(player1);
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A basic land does not cancel nonbasic landwalk when both lands are controlled")
    void cannotBeBlockedWithMixedLands() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GodlessShrine());
        Permanent blocker = addCreatureReady(player2, new WildCantor());
        Permanent attacker = addCreatureReady(player1, new DryadSophisticate());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
