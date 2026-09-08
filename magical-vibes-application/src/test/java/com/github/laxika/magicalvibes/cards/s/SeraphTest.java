package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.f.FolkOfThePines;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldUnderControl;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Seraph.class, BalduvianBears.class, DarkBanishing.class, FolkOfThePines.class,
        GiantGrowth.class, Shyft.class})
class SeraphTest extends BaseCardTest {

    /** Seraph blocks and kills a Balduvian Bears in combat, leaving the trigger resolved. */
    private void seraphKillsBearsInCombat() {
        addCreatureReady(player1, new Seraph());
        addCreatureReady(player2, new BalduvianBears());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(player2, TurnStep.END_STEP);
    }

    private void seraphAndShyftDieInCombat() {
        addCreatureReady(player1, new Seraph());
        addCreatureReady(player2, new Shyft());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(player2, TurnStep.END_STEP);
    }

    @Test
    @DisplayName("A creature Seraph kills returns under Seraph's controller at the next end step")
    void returnsDamagedCreatureUnderControlAtEndStep() {
        seraphKillsBearsInCombat();

        harness.assertOnBattlefield(player1, "Balduvian Bears");
        harness.assertNotInGraveyard(player2, "Balduvian Bears");
        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).isEmpty();
    }

    @Test
    @DisplayName("Seraph returns a creature that dies at the same time as Seraph")
    void returnsShyftWhenSeraphDiesAtTheSameTime() {
        seraphAndShyftDieInCombat();

        harness.assertInGraveyard(player1, "Seraph");
        harness.assertOnBattlefield(player1, "Shyft");
    }

    @Test
    @DisplayName("A creature Seraph did not damage does not return when it dies")
    void noReturnForUndamagedCreature() {
        harness.addToBattlefield(player1, new Seraph());
        Permanent bears = addCreatureReady(player2, new BalduvianBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Balduvian Bears");
        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).isEmpty();

        harness.passUntil(player1, TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("A creature Seraph damages earlier in the turn returns when it dies later")
    void returnsCreatureDamagedEarlierInTurnWhenItDiesLater() {
        addCreatureReady(player1, new Seraph());
        Permanent folk = addCreatureReady(player2, new FolkOfThePines());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(player2, TurnStep.END_OF_COMBAT);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(folk);

        harness.setHand(player2, List.of(new DarkBanishing()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, folk.getId());
        harness.passUntil(player2, TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Folk of the Pines");
        harness.assertNotInGraveyard(player2, "Folk of the Pines");
    }

    @Test
    @DisplayName("Losing control of Seraph makes its controller sacrifice the returned creature")
    void sacrificesReturnedCreatureOnControlLoss() {
        seraphKillsBearsInCombat();

        harness.assertOnBattlefield(player1, "Balduvian Bears");

        Permanent seraph = findPermanent(player1, "Seraph");
        gd.playerBattlefields.get(player1.getId()).remove(seraph);
        gd.playerBattlefields.get(player2.getId()).add(seraph);

        Permanent returnedBears = findPermanent(player1, "Balduvian Bears");
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castInstant(player2, 0, returnedBears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Changing Seraph's controller before return does not sacrifice the returned creature immediately")
    void changingControlBeforeReturnDoesNotSacrificeReturnedCreatureImmediately() {
        addCreatureReady(player1, new Seraph());
        addCreatureReady(player2, new BalduvianBears());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(player2, TurnStep.END_OF_COMBAT);
        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);

        Permanent seraph = findPermanent(player1, "Seraph");
        gd.playerBattlefields.get(player1.getId()).remove(seraph);
        gd.playerBattlefields.get(player2.getId()).add(seraph);

        harness.passUntil(player2, TurnStep.END_STEP);
        harness.runStateBasedActions();
        harness.assertOnBattlefield(player1, "Balduvian Bears");

        gd.playerBattlefields.get(player2.getId()).remove(seraph);
        gd.playerBattlefields.get(player1.getId()).add(seraph);
        harness.runStateBasedActions();
        harness.assertOnBattlefield(player1, "Balduvian Bears");

        gd.playerBattlefields.get(player1.getId()).remove(seraph);
        gd.playerBattlefields.get(player2.getId()).add(seraph);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertInGraveyard(player2, "Balduvian Bears");
    }
}
