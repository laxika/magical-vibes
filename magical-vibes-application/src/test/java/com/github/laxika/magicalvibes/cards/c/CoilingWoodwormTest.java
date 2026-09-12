package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoilingWoodworm.class, Forest.class, Mountain.class})
class CoilingWoodwormTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of Forests on the battlefield; toughness stays 1")
    void powerEqualsForestsOnBattlefield() {
        Permanent woodworm = addCreatureReady(player1, new CoilingWoodworm());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());

        assertThat(gqs.getEffectivePower(gd, woodworm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, woodworm)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power updates as Forests enter and leave the battlefield")
    void powerUpdatesWhenForestsChange() {
        Permanent woodworm = addCreatureReady(player1, new CoilingWoodworm());

        assertThat(gqs.getEffectivePower(gd, woodworm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, woodworm)).isEqualTo(1);

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        assertThat(gqs.getEffectivePower(gd, woodworm)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Forest"));
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getCard().getName().equals("Forest"));
        assertThat(gqs.getEffectivePower(gd, woodworm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, woodworm)).isEqualTo(1);
    }

}
