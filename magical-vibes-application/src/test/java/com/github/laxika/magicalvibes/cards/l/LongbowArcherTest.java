package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.p.Python;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AirElemental.class, LongbowArcher.class, Python.class})
class LongbowArcherTest extends BaseCardTest {

    @Test
    @DisplayName("Reach lets Longbow Archer block a creature with flying")
    void reachCanBlockFlyer() {
        Permanent flyer = addCreatureReady(player1, new AirElemental());
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
        Permanent flyer = addCreatureReady(player1, new AirElemental());
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

        resolveCombat();

        harness.assertOnBattlefield(player1, "Longbow Archer");
        harness.assertInGraveyard(player2, "Python");
    }

    @Test
    @DisplayName("First strike defeats a 3/2 blocker before it deals combat damage")
    void firstStrikeDealsCombatDamageFirstUpstreamReview() {
        addCreatureReady(player1, new LongbowArcher());
        addCreatureReady(player2, new Python());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Longbow Archer");
        harness.assertInGraveyard(player2, "Python");
    }

    @Test
    @DisplayName("First strike kills a non-first-striking attacker before it damages the Archer")
    void firstStrikeWhileBlocking() {
        addCreatureReady(player1, new Python());
        addCreatureReady(player2, new LongbowArcher());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Python");
        harness.assertOnBattlefield(player2, "Longbow Archer");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Reach does not stop a creature without flying from blocking the Archer")
    void reachDoesNotGrantFlying() {
        addCreatureReady(player1, new LongbowArcher());
        Permanent python = addCreatureReady(player2, new Python());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(python.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A flyer surviving first-strike damage still deals normal combat damage")
    void survivingFlyerKillsBlockingArcher() {
        addCreatureReady(player1, new AirElemental());
        addCreatureReady(player2, new LongbowArcher());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Air Elemental");
        harness.assertInGraveyard(player2, "Longbow Archer");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An unblocked Archer deals damage only once")
    void firstStrikeIsNotDoubleStrike() {
        addCreatureReady(player1, new LongbowArcher());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
