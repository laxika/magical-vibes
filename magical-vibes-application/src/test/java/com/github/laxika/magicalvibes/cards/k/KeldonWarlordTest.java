package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KeldonWarlord.class, GrizzlyBears.class, WallOfWood.class, Forest.class})
class KeldonWarlordTest extends BaseCardTest {

    @Test
    @DisplayName("Keldon Warlord is 1/1 when it is your only creature")
    void isOneOneWhenOnlyCreature() {
        Permanent warlord = addCreatureReady(player1, new KeldonWarlord());

        assertThat(gqs.getEffectivePower(gd, warlord)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, warlord)).isEqualTo(1);
    }

    @Test
    @DisplayName("Keldon Warlord power and toughness equal non-Wall creatures you control")
    void ptEqualsNonWallControlledCreatures() {
        Permanent warlord = addCreatureReady(player1, new KeldonWarlord());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, warlord)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warlord)).isEqualTo(3);
    }

    @Test
    @DisplayName("Keldon Warlord does not count Walls you control")
    void doesNotCountWalls() {
        Permanent warlord = addCreatureReady(player1, new KeldonWarlord());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new WallOfWood());

        assertThat(gqs.getEffectivePower(gd, warlord)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warlord)).isEqualTo(2);
    }

    @Test
    @DisplayName("Keldon Warlord does not count noncreature permanents you control")
    void doesNotCountNoncreatures() {
        Permanent warlord = addCreatureReady(player1, new KeldonWarlord());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, warlord)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, warlord)).isEqualTo(1);
    }

    @Test
    @DisplayName("Keldon Warlord counts only your creatures, not opponent creatures")
    void countsOnlyControllersCreatures() {
        Permanent warlord = addCreatureReady(player1, new KeldonWarlord());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, warlord)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, warlord)).isEqualTo(1);
    }
}
