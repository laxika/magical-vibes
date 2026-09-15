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
        flyer.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new AncientSpider());

        prepareDeclareBlockers();

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

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
