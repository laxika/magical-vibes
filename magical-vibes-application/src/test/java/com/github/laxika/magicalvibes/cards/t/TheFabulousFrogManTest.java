package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheFabulousFrogMan.class, CloudSprite.class})
class TheFabulousFrogManTest extends BaseCardTest {

    @Test
    @DisplayName("The Fabulous Frog-Man can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent attacker = addReady(player1, new CloudSprite());
        attacker.setAttacking(true);
        Permanent blocker = addReady(player2, new TheFabulousFrogMan());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
