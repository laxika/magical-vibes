package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrashOfRhinoBeetles.class, Forest.class})
class CrashOfRhinoBeetlesTest extends BaseCardTest {

    @Test
    void remainsFiveFiveWithFewerThanTenLands() {
        addLands(player1, 9);
        Permanent beetles = harness.addToBattlefieldAndReturn(player1, new CrashOfRhinoBeetles());

        assertThat(gqs.getEffectivePower(gd, beetles)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, beetles)).isEqualTo(5);
    }

    @Test
    void getsPlusTenPlusTenAtTenLands() {
        addLands(player1, 10);
        Permanent beetles = harness.addToBattlefieldAndReturn(player1, new CrashOfRhinoBeetles());

        assertThat(gqs.getEffectivePower(gd, beetles)).isEqualTo(15);
        assertThat(gqs.getEffectiveToughness(gd, beetles)).isEqualTo(15);
    }

    @Test
    void losesBoostWhenControllerDropsBelowTenLands() {
        addLands(player1, 10);
        Permanent beetles = harness.addToBattlefieldAndReturn(player1, new CrashOfRhinoBeetles());

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Forest"));

        assertThat(gqs.getEffectivePower(gd, beetles)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, beetles)).isEqualTo(5);
    }

    @Test
    void opponentsLandsDoNotCount() {
        addLands(player2, 10);
        Permanent beetles = harness.addToBattlefieldAndReturn(player1, new CrashOfRhinoBeetles());

        assertThat(gqs.getEffectivePower(gd, beetles)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, beetles)).isEqualTo(5);
    }

    private void addLands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }
}
