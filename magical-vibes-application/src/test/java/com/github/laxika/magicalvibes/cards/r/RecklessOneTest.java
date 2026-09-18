package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.n.NosyGoblin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RecklessOne.class, NosyGoblin.class, ElvishWarrior.class})
class RecklessOneTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of Goblins on the battlefield")
    void ptEqualsBattlefieldGoblinCount() {
        Permanent recklessOne = addCreatureReady(player1, new RecklessOne());
        harness.addToBattlefield(player1, new NosyGoblin());
        harness.addToBattlefield(player2, new NosyGoblin());
        harness.addToBattlefield(player1, new ElvishWarrior());

        assertThat(gqs.getEffectivePower(gd, recklessOne)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recklessOne)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power and toughness update when Goblins enter and leave the battlefield")
    void ptUpdatesWithGoblinCount() {
        Permanent recklessOne = addCreatureReady(player1, new RecklessOne());
        assertThat(gqs.getEffectivePower(gd, recklessOne)).isEqualTo(1);

        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new NosyGoblin());
        assertThat(gqs.getEffectivePower(gd, recklessOne)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, recklessOne)).isEqualTo(2);

        gd.playerBattlefields.get(player2.getId()).remove(goblin);
        assertThat(gqs.getEffectivePower(gd, recklessOne)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, recklessOne)).isEqualTo(1);
    }
}
