package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;

class GrapplerSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Grappler Spider can block a creature with flying")
    void canBlockFlyingCreature() {
        addReadyPermanent(player2, new GrapplerSpider());
        addReadyAttacker(player1, new AirElemental());

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Grappler Spider can also block a non-flying creature")
    void canBlockNonFlyingCreature() {
        addReadyPermanent(player2, new GrapplerSpider());
        addReadyAttacker(player1, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyAttacker(Player player, Card card) {
        Permanent permanent = addReadyPermanent(player, card);
        permanent.setAttacking(true);
        return permanent;
    }
}
