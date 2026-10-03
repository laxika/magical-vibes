package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.MoggJailer;
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

@CardUsed({AncientSpider.class, AuroraGriffin.class, MoggJailer.class})
class AncientSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Reach lets Ancient Spider block a creature with flying")
    void reachCanBlockFlyer() {
        Permanent flyer = addCreatureReady(player1, new AuroraGriffin());
        Permanent spider = addCreatureReady(player2, new AncientSpider());

        declareAttackersAndPrepareBlockers(List.of(indexOf(player1, flyer)));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, spider), indexOf(player1, flyer))));

        assertThat(spider.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("First strike destroys a 2/2 blocker before it deals combat damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent spider = addCreatureReady(player1, new AncientSpider());
        spider.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MoggJailer());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spider);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
        assertThat(spider.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Ancient Spider kills a flying attacker before it can deal damage")
    void firstStrikeWhileBlockingFlyer() {
        Permanent flyer = addCreatureReady(player1, new AuroraGriffin());
        Permanent spider = addCreatureReady(player2, new AncientSpider());

        declareAttackersAndPrepareBlockers(List.of(indexOf(player1, flyer)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, spider), indexOf(player1, flyer))));
        resolveCombat();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(flyer.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(spider);
        assertThat(spider.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An unblocked Ancient Spider deals damage only in the first-strike step")
    void unblockedFirstStrikeDoesNotDealRegularDamage() {
        Permanent spider = addCreatureReady(player1, new AncientSpider());

        declareAttackersAndPrepareBlockers(List.of(indexOf(player1, spider)));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
