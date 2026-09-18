package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.g.GoblinSkyRaider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrightstoneRitual.class, GoblinSkyRaider.class, GlorySeeker.class})
class BrightstoneRitualTest extends BaseCardTest {

    @Test
    @DisplayName("Adds red mana for each Goblin on the battlefield")
    void addsRedManaForEachGoblinOnBattlefield() {
        harness.addToBattlefield(player1, new GoblinSkyRaider());
        harness.addToBattlefield(player2, new GoblinSkyRaider());
        harness.addToBattlefield(player2, new GoblinSkyRaider());

        harness.castFromHand(player1, new BrightstoneRitual(), "{R}");
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ignores non-Goblins on the battlefield")
    void ignoresNonGoblins() {
        harness.addToBattlefield(player1, new GlorySeeker());

        harness.castFromHand(player1, new BrightstoneRitual(), "{R}");
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Counts Goblins that enter the battlefield before resolution")
    void countsGoblinsAtResolution() {
        harness.addToBattlefield(player1, new GoblinSkyRaider());
        harness.castFromHand(player1, new BrightstoneRitual(), "{R}");

        harness.addToBattlefield(player2, new GoblinSkyRaider());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }
}
