package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WayfaringGiant.class, Forest.class, Island.class, Plains.class, Swamp.class, Mountain.class})
class WayfaringGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Wayfaring Giant gets +1/+1 for each distinct basic land type you control")
    void boostsByDomainCount() {
        Permanent giant = addCreatureReady(player1, new WayfaringGiant());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(6);
    }

    @Test
    @DisplayName("Duplicate basic types and opponent lands do not raise the Domain count")
    void countsDistinctControllerTypesOnly() {
        Permanent giant = addCreatureReady(player1, new WayfaringGiant());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(4);
    }

    @Test
    @DisplayName("Each of the five basic land types contributes one to Domain")
    void countsAllFiveBasicLandTypes() {
        Permanent giant = addCreatureReady(player1, new WayfaringGiant());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(8);
    }

    @Test
    @DisplayName("Wayfaring Giant updates when its controller's lands change")
    void updatesWhenLandsChange() {
        Permanent giant = addCreatureReady(player1, new WayfaringGiant());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Swamp());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(5);
    }
}
