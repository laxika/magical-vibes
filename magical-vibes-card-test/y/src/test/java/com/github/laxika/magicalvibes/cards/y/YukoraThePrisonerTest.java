package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.m.MirrorGallery;
import com.github.laxika.magicalvibes.cards.n.NinjaOfTheDeepHours;
import com.github.laxika.magicalvibes.cards.o.OgreMarauder;
import com.github.laxika.magicalvibes.cards.z.ZulaportCutthroat;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Yukora, the Prisoner")
@CardUsed({YukoraThePrisoner.class, GnarledMass.class, NinjaOfTheDeepHours.class,
        OgreMarauder.class, MirrorGallery.class, ZulaportCutthroat.class})
class YukoraThePrisonerTest extends BaseCardTest {

    @Test
    @DisplayName("All non-Ogre creatures you control are sacrificed when Yukora dies")
    void nonOgreCreaturesSacrificedOnDeath() {
        harness.addToBattlefield(player1, new GnarledMass());
        harness.addToBattlefield(player1, new NinjaOfTheDeepHours());
        harness.addToBattlefield(player2, new GnarledMass());

        killYukora();

        harness.assertInGraveyard(player1, "Gnarled Mass");
        harness.assertInGraveyard(player1, "Ninja of the Deep Hours");
        // Only your own creatures are sacrificed.
        harness.assertOnBattlefield(player2, "Gnarled Mass");
    }

    @Test
    @DisplayName("Ogres you control survive")
    void ogresSurvive() {
        harness.addToBattlefield(player1, new OgreMarauder());
        harness.addToBattlefield(player1, new GnarledMass());

        killYukora();

        harness.assertOnBattlefield(player1, "Ogre Marauder");
        harness.assertInGraveyard(player1, "Gnarled Mass");
    }

    @Test
    @DisplayName("Noncreature permanents survive")
    void noncreaturePermanentsSurvive() {
        harness.addToBattlefield(player1, new MirrorGallery());
        harness.addToBattlefield(player1, new OgreMarauder());

        killYukora();

        harness.assertOnBattlefield(player1, "Mirror Gallery");
        harness.assertOnBattlefield(player1, "Ogre Marauder");
    }

    @Test
    @DisplayName("Trigger also fires when Yukora is exiled instead of dying")
    void triggerFiresOnExile() {
        harness.addToBattlefield(player1, new GnarledMass());
        Permanent yukora = harness.addToBattlefieldAndReturn(player1, new YukoraThePrisoner());
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToExile(harness.getGameData(), yukora));

        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Gnarled Mass");
    }

    @Test
    @DisplayName("Creatures entering before the leave trigger resolves are also sacrificed")
    void sacrificesCreaturesPresentAtResolution() {
        Permanent yukora = harness.addToBattlefieldAndReturn(player1, new YukoraThePrisoner());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToExile(gd, yukora));
        harness.addToBattlefield(player1, new GnarledMass());
        harness.addToBattlefield(player1, new OgreMarauder());

        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Gnarled Mass");
        harness.assertOnBattlefield(player1, "Ogre Marauder");
    }

    @Test
    @DisplayName("Another Yukora entering before resolution is also sacrificed")
    void anotherYukoraIsAlsoSacrificed() {
        Permanent yukora = harness.addToBattlefieldAndReturn(player1, new YukoraThePrisoner());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToExile(gd, yukora));
        harness.addToBattlefield(player1, new YukoraThePrisoner());
        harness.addToBattlefield(player1, new OgreMarauder());

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Yukora, the Prisoner");
        harness.assertInGraveyard(player1, "Yukora, the Prisoner");
        harness.assertOnBattlefield(player1, "Ogre Marauder");
    }

    @Test
    @DisplayName("Sacrificed creatures see the other creatures die simultaneously")
    void sacrificesCreaturesSimultaneously() {
        harness.addToBattlefield(player1, new ZulaportCutthroat());
        harness.addToBattlefield(player1, new GnarledMass());
        Permanent yukora = harness.addToBattlefieldAndReturn(player1, new YukoraThePrisoner());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToExile(gd, yukora));

        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Zulaport Cutthroat");
        harness.assertInGraveyard(player1, "Gnarled Mass");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    private void killYukora() {
        Permanent yukora = harness.addToBattlefieldAndReturn(player1, new YukoraThePrisoner());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(harness.getGameData(), yukora));
        resolveAllTriggers();
    }
}
