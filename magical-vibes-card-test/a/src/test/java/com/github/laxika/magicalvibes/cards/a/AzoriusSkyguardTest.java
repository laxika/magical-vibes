package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AzoriusSkyguard.class, GrizzlyBears.class})
class AzoriusSkyguardTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures your opponents control get -1/-0")
    void debuffsOpponentCreatures() {
        harness.addToBattlefield(player1, new AzoriusSkyguard());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not affect creatures you control")
    void doesNotAffectOwnCreatures() {
        harness.addToBattlefield(player1, new AzoriusSkyguard());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
    }

    @Test
    void multipleSkyguardsStackTheirDebuffs() {
        harness.addToBattlefield(player1, new AzoriusSkyguard());
        harness.addToBattlefield(player1, new AzoriusSkyguard());
        Permanent opponentSkyguard = harness.addToBattlefieldAndReturn(player2, new AzoriusSkyguard());

        assertThat(gqs.getEffectivePower(gd, opponentSkyguard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentSkyguard)).isEqualTo(3);
    }

    @Test
    void opposingSkyguardsDebuffEachOtherButNotThemselves() {
        Permanent ownSkyguard = harness.addToBattlefieldAndReturn(player1, new AzoriusSkyguard());
        Permanent opponentSkyguard = harness.addToBattlefieldAndReturn(player2, new AzoriusSkyguard());

        assertThat(gqs.getEffectivePower(gd, ownSkyguard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownSkyguard)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentSkyguard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentSkyguard)).isEqualTo(3);
    }

    @Test
    void debuffsCanReducePowerBelowZeroWithoutReducingToughness() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new AzoriusSkyguard());
        }
        Permanent opponentSkyguard = harness.addToBattlefieldAndReturn(player2, new AzoriusSkyguard());

        assertThat(gqs.getEffectivePower(gd, opponentSkyguard)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, opponentSkyguard)).isEqualTo(3);
        harness.runStateBasedActions();
        harness.assertOnBattlefield(player2, "Azorius Skyguard");
    }
}
