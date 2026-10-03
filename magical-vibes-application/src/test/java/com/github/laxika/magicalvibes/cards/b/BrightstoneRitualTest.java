package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.g.GoblinSkyRaider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    @Test
    @DisplayName("Ignores Goblins in hands, libraries, graveyards, and exile")
    void ignoresGoblinsOutsideBattlefield() {
        harness.addToBattlefield(player1, new GoblinSkyRaider());
        harness.setHand(player2, List.of(new GoblinSkyRaider()));
        harness.setLibrary(player1, List.of(new GoblinSkyRaider()));
        harness.setGraveyard(player1, List.of(new GoblinSkyRaider()));
        harness.setExile(player2, List.of(new GoblinSkyRaider()));

        harness.castFromHand(player1, new BrightstoneRitual(), "{R}");
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Adds mana only to the caster, including for tapped opposing Goblins")
    void addsManaOnlyToCaster() {
        harness.addToBattlefield(player1, new GoblinSkyRaider());
        gd.playerBattlefields.get(player1.getId()).getFirst().setTapped(true);
        harness.addToBattlefield(player2, new GoblinSkyRaider());

        harness.castFromHand(player2, new BrightstoneRitual(), "{R}");
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }
}
