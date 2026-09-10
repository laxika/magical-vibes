package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.ShuFootSoldiers;
import com.github.laxika.magicalvibes.cards.w.WeiInfantry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({GuanYus1000LiMarch.class, ShuFootSoldiers.class, WeiInfantry.class, Plains.class})
class GuanYus1000LiMarchTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys tapped creatures on both sides")
    void destroysTappedCreatures() {
        Permanent shuFootSoldiers = harness.addToBattlefieldAndReturn(player1, new ShuFootSoldiers());
        shuFootSoldiers.tap();
        Permanent weiInfantry = harness.addToBattlefieldAndReturn(player2, new WeiInfantry());
        weiInfantry.tap();

        harness.castFromHand(player1, new GuanYus1000LiMarch(), "{4}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Shu Foot Soldiers");
        harness.assertNotOnBattlefield(player2, "Wei Infantry");
        harness.assertInGraveyard(player1, "Shu Foot Soldiers");
        harness.assertInGraveyard(player2, "Wei Infantry");
    }

    @Test
    @DisplayName("Untapped creatures are not destroyed")
    void untappedCreaturesSurvive() {
        harness.addToBattlefield(player2, new ShuFootSoldiers());

        harness.castFromHand(player1, new GuanYus1000LiMarch(), "{4}{W}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Shu Foot Soldiers");
    }

    @Test
    @DisplayName("Indestructible tapped creature survives")
    void indestructibleTappedCreatureSurvives() {
        Permanent shuFootSoldiers = harness.addToBattlefieldAndReturn(player2, new ShuFootSoldiers());
        shuFootSoldiers.tap();
        shuFootSoldiers.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        harness.castFromHand(player1, new GuanYus1000LiMarch(), "{4}{W}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Shu Foot Soldiers");
    }

    @Test
    @DisplayName("Tapped noncreature permanents are not destroyed")
    void tappedNoncreaturePermanentsSurvive() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        plains.tap();

        harness.castFromHand(player1, new GuanYus1000LiMarch(), "{4}{W}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Plains");
    }
}
