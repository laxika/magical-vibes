package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RootSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking gives Root Spider +1/+0 and first strike")
    void blockingBoostsAndGrantsFirstStrike() {
        addReadyBears(player1).setAttacking(true);
        Permanent spider = addReadySpider(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(spider.getPowerModifier()).isEqualTo(1);
        assertThat(spider.getToughnessModifier()).isZero();
        assertThat(spider.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Becoming blocked does not trigger Root Spider")
    void becomingBlockedDoesNothing() {
        Permanent spider = addReadySpider(player1);
        spider.setAttacking(true);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(spider.getPowerModifier()).isZero();
        assertThat(spider.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    private Permanent addReadySpider(Player player) {
        Permanent permanent = new Permanent(new RootSpider());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyBears(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
