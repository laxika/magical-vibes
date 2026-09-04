package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.e.EarthElemental;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({WrathOfGod.class, DrudgeSkeletons.class, EarthElemental.class, HowlingMine.class})
class WrathOfGodTest extends BaseCardTest {

    @Test
    @CardUsed(EarthElemental.class)
    @DisplayName("Destroys all creatures controlled by both players")
    void destroysAllCreaturesControlledByBothPlayers() {
        harness.addToBattlefield(player1, new EarthElemental());
        harness.addToBattlefield(player2, new EarthElemental());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Earth Elemental");
        harness.assertNotOnBattlefield(player2, "Earth Elemental");
        harness.assertInGraveyard(player1, "Earth Elemental");
        harness.assertInGraveyard(player2, "Earth Elemental");
    }

    @Test
    @CardUsed({EarthElemental.class, HowlingMine.class})
    @DisplayName("Does not destroy noncreature permanents")
    void doesNotDestroyNoncreaturePermanents() {
        harness.addToBattlefield(player1, new EarthElemental());
        harness.addToBattlefield(player1, new HowlingMine());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Earth Elemental");
        harness.assertInGraveyard(player1, "Earth Elemental");
        harness.assertOnBattlefield(player1, "Howling Mine");
        harness.assertNotInGraveyard(player1, "Howling Mine");
    }

    @Test
    @CardUsed(DrudgeSkeletons.class)
    @DisplayName("Destroyed creatures cannot be regenerated")
    void destroyedCreaturesCannotBeRegenerated() {
        Permanent skeletons = harness.addToBattlefieldAndReturn(player1, new DrudgeSkeletons());
        skeletons.setRegenerationShield(1);

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Drudge Skeletons");
        harness.assertInGraveyard(player1, "Drudge Skeletons");
    }
}
