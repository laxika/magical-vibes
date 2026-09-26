package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AmrouKithkin;
import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.b.BlackManaBattery;
import com.github.laxika.magicalvibes.cards.w.WalkingDead;
import com.github.laxika.magicalvibes.cards.w.WallOfPutridFlesh;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({Cleanse.class, WallOfPutridFlesh.class, BarbaryApes.class, AmrouKithkin.class,
        BlackManaBattery.class, WalkingDead.class})
class CleanseTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all black creatures on both battlefields")
    void destroysAllBlackCreatures() {
        harness.addToBattlefield(player1, new WallOfPutridFlesh());
        harness.addToBattlefield(player2, new WallOfPutridFlesh());
        harness.addToBattlefield(player1, new BarbaryApes());
        harness.addToBattlefield(player2, new BarbaryApes());

        castCleanse();

        harness.assertNotOnBattlefield(player1, "Wall of Putrid Flesh");
        harness.assertNotOnBattlefield(player2, "Wall of Putrid Flesh");
        harness.assertOnBattlefield(player1, "Barbary Apes");
        harness.assertOnBattlefield(player2, "Barbary Apes");
    }

    @Test
    @DisplayName("Leaves nonblack creatures and noncreature permanents alone")
    void leavesNonblackCreaturesAndNoncreaturesAlone() {
        harness.addToBattlefield(player1, new AmrouKithkin());
        harness.addToBattlefield(player2, new BarbaryApes());
        harness.addToBattlefield(player2, new BlackManaBattery());
        harness.addToBattlefield(player2, new WallOfPutridFlesh());

        castCleanse();

        harness.assertOnBattlefield(player1, "Amrou Kithkin");
        harness.assertOnBattlefield(player2, "Barbary Apes");
        harness.assertOnBattlefield(player2, "Black Mana Battery");
        harness.assertNotOnBattlefield(player2, "Wall of Putrid Flesh");
    }

    @Test
    @DisplayName("Allows a regeneration shield to save a black creature")
    void regeneratingBlackCreatureSurvives() {
        harness.addToBattlefield(player2, new WalkingDead());
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        castCleanse();

        harness.assertOnBattlefield(player2, "Walking Dead");
        harness.assertNotInGraveyard(player2, "Walking Dead");
    }

    private void castCleanse() {
        harness.castFromHand(player1, new Cleanse(), "{2}{W}{W}");
        harness.passBothPriorities();
    }
}
