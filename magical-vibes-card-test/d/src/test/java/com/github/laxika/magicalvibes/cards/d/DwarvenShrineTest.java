package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({DwarvenShrine.class, DwarvenGrunt.class})
class DwarvenShrineTest extends BaseCardTest {

    @Test
    @DisplayName("Deals twice the matching graveyard count to the spell's caster")
    void dealsTwiceMatchingGraveyardCountToCaster() {
        harness.addToBattlefield(player1, new DwarvenShrine());
        harness.setGraveyard(player1, List.of(new DwarvenGrunt()));
        harness.setGraveyard(player2, List.of(new DwarvenGrunt(), new DwarvenShrine()));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new DwarvenGrunt(), "{R}");
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Dwarven Grunt");
    }

    @Test
    @DisplayName("Deals no damage when no matching graveyard card exists")
    void noMatchingCardsMeansNoDamage() {
        harness.addToBattlefield(player1, new DwarvenShrine());
        harness.setGraveyard(player1, List.of(new DwarvenShrine()));
        harness.setGraveyard(player2, List.of(new DwarvenShrine()));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new DwarvenGrunt(), "{R}");
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Dwarven Grunt");
    }

    @Test
    @DisplayName("Counts matching graveyard cards when the trigger resolves")
    void countsMatchingCardsAtResolution() {
        harness.addToBattlefield(player1, new DwarvenShrine());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new DwarvenGrunt(), "{R}");
        harness.setGraveyard(player1, List.of(new DwarvenGrunt(), new DwarvenGrunt()));
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Dwarven Grunt");
    }

    @Test
    @DisplayName("Damages its own controller when that player casts a matching spell")
    void damagesItsOwnController() {
        harness.addToBattlefield(player1, new DwarvenShrine());
        harness.setGraveyard(player2, List.of(new DwarvenGrunt()));

        harness.castFromHand(player1, new DwarvenGrunt(), "{R}");
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Dwarven Grunt");
    }

    @Test
    @DisplayName("Deals no damage if matching cards leave graveyards before resolution")
    void matchingCardsRemovedBeforeResolution() {
        harness.addToBattlefield(player1, new DwarvenShrine());
        harness.setGraveyard(player1, List.of(new DwarvenGrunt()));
        harness.setGraveyard(player2, List.of(new DwarvenGrunt()));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new DwarvenGrunt(), "{R}");
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Dwarven Grunt");
    }

    @Test
    @DisplayName("Each Shrine damages the caster independently")
    void multipleShrinesTriggerIndependently() {
        harness.addToBattlefield(player1, new DwarvenShrine());
        harness.addToBattlefield(player2, new DwarvenShrine());
        harness.setGraveyard(player1, List.of(new DwarvenGrunt()));

        harness.castFromHand(player1, new DwarvenGrunt(), "{R}");
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Dwarven Grunt");
    }

    @Test
    @DisplayName("A Shrine spell does not trigger its own ability before entering")
    void doesNotTriggerForItsOwnCast() {
        harness.setGraveyard(player1, List.of(new DwarvenShrine()));

        harness.castFromHand(player1, new DwarvenShrine(), "{1}{R}{R}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Dwarven Shrine");
    }

    @Test
    @DisplayName("Also triggers for a noncreature spell")
    void triggersForNoncreatureSpell() {
        harness.addToBattlefield(player1, new DwarvenShrine());
        harness.setGraveyard(player1, List.of(new DwarvenShrine()));
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new DwarvenShrine(), "{1}{R}{R}");
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Dwarven Shrine");
    }
}
