package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed({TerritorialMaro.class, Forest.class, Island.class, Mountain.class, Plains.class, Swamp.class, TangledIslet.class})
class TerritorialMaroTest extends BaseCardTest {

    @Test
    @DisplayName("P/T equals twice the number of distinct basic land types you control")
    void ptEqualsTwiceDomainCount() {
        Permanent maro = harness.addToBattlefieldAndReturn(player1, new TerritorialMaro());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, maro)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, maro)).isEqualTo(6);
    }

    @Test
    @DisplayName("Duplicate types and opponent lands do not raise the Domain count")
    void countsDistinctControllerTypesOnly() {
        Permanent maro = harness.addToBattlefieldAndReturn(player1, new TerritorialMaro());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, maro)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, maro)).isEqualTo(2);
    }

    @Test
    @DisplayName("P/T updates dynamically as the controller's basic land types change")
    void ptUpdatesWhenLandsChange() {
        Permanent maro = harness.addToBattlefieldAndReturn(player1, new TerritorialMaro());

        assertThat(gqs.getEffectivePower(gd, maro)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, maro)).isZero();

        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Swamp());

        assertThat(gqs.getEffectivePower(gd, maro)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, maro)).isEqualTo(4);
    }

    @Test
    void nonbasicDualLandContributesBothTypesWithoutDoubleCounting() {
        Permanent maro = harness.addToBattlefieldAndReturn(player1, new TerritorialMaro());
        harness.addToBattlefield(player1, new TangledIslet());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, maro)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, maro)).isEqualTo(4);
    }

    @Test
    void allFiveBasicLandTypesGiveTenPowerAndToughness() {
        Permanent maro = harness.addToBattlefieldAndReturn(player1, new TerritorialMaro());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Swamp());

        assertThat(gqs.getEffectivePower(gd, maro)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, maro)).isEqualTo(10);
    }

    @Test
    void losingLastLandCausesMaroToDieFromZeroToughness() {
        Permanent maro = harness.addToBattlefieldAndReturn(player1, new TerritorialMaro());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        assertThat(gqs.getEffectiveToughness(gd, maro)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(forest);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(maro);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(maro.getCard());
    }

    @Test
    void characteristicAbilityWorksInHandAndGraveyardUsingOwnersLands() {
        TerritorialMaro maro = new TerritorialMaro();
        harness.setHand(player1, java.util.List.of(maro));
        harness.addToBattlefield(player1, new TangledIslet());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectiveCardPower(gd, maro)).isEqualTo(4);
        assertThat(gqs.getEffectiveCardToughness(gd, maro)).isEqualTo(4);

        gd.playerHands.get(player1.getId()).remove(maro);
        gd.playerGraveyards.get(player1.getId()).add(maro);
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectiveCardPower(gd, maro)).isEqualTo(6);
        assertThat(gqs.getEffectiveCardToughness(gd, maro)).isEqualTo(6);
    }
}
