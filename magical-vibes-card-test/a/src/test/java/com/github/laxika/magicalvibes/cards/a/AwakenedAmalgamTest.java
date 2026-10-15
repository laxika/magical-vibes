package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

@CardUsed({AwakenedAmalgam.class, Forest.class, Island.class, Mountain.class, GrizzlyBears.class})
class AwakenedAmalgamTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of differently named lands you control")
    void powerAndToughnessEqualDifferentlyNamedControlledLands() {
        Permanent amalgam = harness.addToBattlefieldAndReturn(player1, new AwakenedAmalgam());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, amalgam)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, amalgam)).isEqualTo(3);
    }

    @Test
    @DisplayName("P/T updates when the set of differently named lands changes")
    void powerAndToughnessUpdateWhenLandNamesChange() {
        Permanent amalgam = harness.addToBattlefieldAndReturn(player1, new AwakenedAmalgam());

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, amalgam)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, amalgam)).isEqualTo(1);

        harness.addToBattlefield(player1, new Island());
        assertThat(gqs.getEffectivePower(gd, amalgam)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, amalgam)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opposing lands and controlled nonlands do not contribute to power and toughness")
    void ignoresOpposingLandsAndControlledNonlands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Mountain());
        Permanent amalgam = harness.addToBattlefieldAndReturn(player1, new AwakenedAmalgam());

        assertThat(gqs.getEffectivePower(gd, amalgam)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, amalgam)).isEqualTo(1);
    }

    @Test
    @DisplayName("Amalgam dies on resolution when its controller has no lands")
    void diesWithNoControlledLands() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new AwakenedAmalgam()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Awakened Amalgam");
        harness.assertInGraveyard(player1, "Awakened Amalgam");
    }
}
