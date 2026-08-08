package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Yukora, the Prisoner")
class YukoraThePrisonerTest extends BaseCardTest {

    @Test
    @DisplayName("All non-Ogre creatures you control are sacrificed when Yukora dies")
    void nonOgreCreaturesSacrificedOnDeath() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GiantSpider());
        harness.addToBattlefield(player2, new GrizzlyBears());

        killYukora();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Giant Spider");
        // Only your own creatures are sacrificed.
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Ogres you control survive")
    void ogresSurvive() {
        harness.addToBattlefield(player1, new GrayOgre());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killYukora();

        harness.assertOnBattlefield(player1, "Gray Ogre");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Trigger also fires when Yukora is exiled instead of dying")
    void triggerFiresOnExile() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new YukoraThePrisoner());

        Permanent yukora = findPermanent();
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToExile(harness.getGameData(), yukora));

        resolveTrigger();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void killYukora() {
        harness.addToBattlefield(player1, new YukoraThePrisoner());
        Permanent yukora = findPermanent();
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(harness.getGameData(), yukora));
        resolveTrigger();
    }

    private Permanent findPermanent() {
        return harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof YukoraThePrisoner)
                .findFirst().orElseThrow();
    }

    private void resolveTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
