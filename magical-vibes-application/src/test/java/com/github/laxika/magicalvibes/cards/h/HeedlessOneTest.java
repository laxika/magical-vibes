package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeedlessOne.class, ElvishWarrior.class, GrizzlyBears.class})
class HeedlessOneTest extends BaseCardTest {

    @Test
    @DisplayName("Heedless One counts itself when it is the only Elf")
    void countsItself() {
        Permanent heedlessOne = addHeedlessOneReady(player1);

        assertThat(gqs.getEffectivePower(gd, heedlessOne)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, heedlessOne)).isEqualTo(1);
    }

    @Test
    @DisplayName("Heedless One counts Elves on both battlefields and ignores other creatures")
    void countsAllElves() {
        Permanent heedlessOne = addHeedlessOneReady(player1);
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.addToBattlefield(player2, new ElvishWarrior());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, heedlessOne)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, heedlessOne)).isEqualTo(3);
    }

    @Test
    @DisplayName("Heedless One updates its power and toughness as Elves enter and leave")
    void updatesWhenElvesChange() {
        Permanent heedlessOne = addHeedlessOneReady(player1);

        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.addToBattlefield(player2, new ElvishWarrior());
        assertThat(gqs.getEffectivePower(gd, heedlessOne)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, heedlessOne)).isEqualTo(3);

        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getCard().getName().equals("Elvish Warrior"));
        assertThat(gqs.getEffectivePower(gd, heedlessOne)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, heedlessOne)).isEqualTo(2);
    }

    private Permanent addHeedlessOneReady(Player player) {
        Permanent permanent = new Permanent(new HeedlessOne());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
