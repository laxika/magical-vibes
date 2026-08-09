package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;

class PlatedSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Plated Spider can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent spider = new Permanent(new PlatedSpider());
        spider.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(spider);

        Permanent flyer = new Permanent(new AirElemental());
        flyer.setSummoningSick(false);
        flyer.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(flyer);

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }
}
