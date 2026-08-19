package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AncientSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Reach lets Ancient Spider block a creature with flying")
    void reachCanBlockFlyer() {
        Permanent flyer = addReadyAttacker(player1, new SuntailHawk());
        Permanent spider = addReadyBlocker(player2, new AncientSpider());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, spider), indexOf(player1, flyer))));

        assertThat(spider.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("First strike destroys a 2/2 blocker before it deals combat damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent spider = addReadyAttacker(player1, new AncientSpider());
        Permanent blocker = addReadyBlocker(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spider);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
    }

    private Permanent addReadyAttacker(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyBlocker(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
