package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AmrouKithkin;
import com.github.laxika.magicalvibes.cards.a.ArenaOfTheAncients;
import com.github.laxika.magicalvibes.cards.b.BlightsteelColossus;
import com.github.laxika.magicalvibes.cards.c.ClergyOfTheHolyNimbus;
import com.github.laxika.magicalvibes.cards.w.WalkingDead;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({Hellfire.class, AmrouKithkin.class, ArenaOfTheAncients.class,
        WalkingDead.class, BlightsteelColossus.class, ClergyOfTheHolyNimbus.class})
class HellfireTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys nonblack creatures and deals three damage plus the number destroyed")
    void destroysNonblackCreaturesAndDealsDamageBasedOnDestroyedCount() {
        harness.addToBattlefield(player1, new AmrouKithkin());
        harness.addToBattlefield(player2, new AmrouKithkin());
        harness.addToBattlefield(player1, new WalkingDead());
        harness.addToBattlefield(player2, new ArenaOfTheAncients());

        castHellfire();

        harness.assertNotOnBattlefield(player1, "Amrou Kithkin");
        harness.assertNotOnBattlefield(player2, "Amrou Kithkin");
        harness.assertOnBattlefield(player1, "Walking Dead");
        harness.assertOnBattlefield(player2, "Arena of the Ancients");
        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Does not count an indestructible creature in the damage")
    void doesNotCountIndestructibleCreature() {
        harness.addToBattlefield(player1, new AmrouKithkin());
        harness.addToBattlefield(player2, new BlightsteelColossus());

        castHellfire();

        harness.assertNotOnBattlefield(player1, "Amrou Kithkin");
        harness.assertOnBattlefield(player2, "Blightsteel Colossus");
        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Does not count a creature that regenerates in the damage")
    void doesNotCountRegeneratedCreature() {
        harness.addToBattlefield(player1, new AmrouKithkin());
        harness.addToBattlefield(player2, new ClergyOfTheHolyNimbus());

        castHellfire();

        harness.assertNotOnBattlefield(player1, "Amrou Kithkin");
        harness.assertOnBattlefield(player2, "Clergy of the Holy Nimbus");
        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Deals only three damage when no nonblack creature dies")
    void dealsOnlyBaseDamageWhenNoCreatureDies() {
        harness.addToBattlefield(player1, new ArenaOfTheAncients());
        harness.addToBattlefield(player2, new WalkingDead());

        castHellfire();

        harness.assertOnBattlefield(player1, "Arena of the Ancients");
        harness.assertOnBattlefield(player2, "Walking Dead");
        harness.assertLife(player1, 17);
    }

    private void castHellfire() {
        harness.castFromHand(player1, new Hellfire(), "{2}{B}{B}{B}");
        harness.passBothPriorities();
    }
}
