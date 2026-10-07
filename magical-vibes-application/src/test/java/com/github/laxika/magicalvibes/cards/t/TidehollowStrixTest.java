package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.j.JungleWeaver;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TidehollowStrix.class, WoollyThoctar.class, JungleWeaver.class})
class TidehollowStrixTest extends BaseCardTest {

    @Test
    void resolvesWithoutAnEnterTheBattlefieldTrigger() {
        harness.setHand(player1, List.of(new TidehollowStrix()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tidehollow Strix");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void groundCreatureCannotBlockFlyingStrix() {
        addCreatureReady(player1, new TidehollowStrix());
        addCreatureReady(player2, new WoollyThoctar());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void flyingCreatureCanBlockStrix() {
        addCreatureReady(player1, new TidehollowStrix());
        Permanent blocker = addCreatureReady(player2, new TidehollowStrix());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        resolveCombat();
        harness.assertInGraveyard(player1, "Tidehollow Strix");
        harness.assertInGraveyard(player2, "Tidehollow Strix");
        harness.assertLife(player2, 20);
    }

    @Test
    void deathtouchDestroysLargerReachBlocker() {
        addCreatureReady(player1, new TidehollowStrix());
        addCreatureReady(player2, new JungleWeaver());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Tidehollow Strix");
        harness.assertInGraveyard(player2, "Jungle Weaver");
        harness.assertNotOnBattlefield(player2, "Jungle Weaver");
        harness.assertLife(player2, 20);
    }

    @Test
    void flyingStrixCanBlockAndKillLargerGroundAttacker() {
        addCreatureReady(player1, new WoollyThoctar());
        addCreatureReady(player2, new TidehollowStrix());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Woolly Thoctar");
        harness.assertNotOnBattlefield(player1, "Woolly Thoctar");
        harness.assertInGraveyard(player2, "Tidehollow Strix");
        harness.assertLife(player2, 20);
    }

    @Test
    void unblockedDeathtouchDamageOnlyReducesPlayerLife() {
        addCreatureReady(player1, new TidehollowStrix());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Tidehollow Strix");
    }
}
