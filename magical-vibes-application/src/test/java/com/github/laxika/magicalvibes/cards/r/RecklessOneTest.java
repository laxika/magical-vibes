package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RecklessOne.class, GoblinPiker.class, GrizzlyBears.class})
class RecklessOneTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of Goblins on the battlefield")
    void ptEqualsBattlefieldGoblinCount() {
        Permanent recklessOne = addRecklessOneReady(player1);
        harness.addToBattlefield(player1, new GoblinPiker());
        harness.addToBattlefield(player2, new GoblinPiker());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, recklessOne)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recklessOne)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power and toughness update when Goblins enter and leave the battlefield")
    void ptUpdatesWithGoblinCount() {
        Permanent recklessOne = addRecklessOneReady(player1);
        assertThat(gqs.getEffectivePower(gd, recklessOne)).isEqualTo(1);

        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new GoblinPiker());
        assertThat(gqs.getEffectivePower(gd, recklessOne)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, recklessOne)).isEqualTo(2);

        gd.playerBattlefields.get(player2.getId()).remove(goblin);
        assertThat(gqs.getEffectivePower(gd, recklessOne)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, recklessOne)).isEqualTo(1);
    }

    private Permanent addRecklessOneReady(Player player) {
        Permanent permanent = new Permanent(new RecklessOne());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
