package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.f.FolkOfThePines;
import com.github.laxika.magicalvibes.cards.f.FyndhornElves;
import com.github.laxika.magicalvibes.cards.w.WordOfUndoing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrovikanVampire.class, BalduvianBears.class, DarkBanishing.class,
        FolkOfThePines.class, FyndhornElves.class, WordOfUndoing.class})
class KrovikanVampireTest extends BaseCardTest {

    /** Krovikan Vampire blocks and kills a Balduvian Bears in combat. */
    private void vampireKillsBearsInCombat() {
        addCreatureReady(player1, new KrovikanVampire());
        addCreatureReady(player2, new BalduvianBears());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);
    }

    private void advanceToEndStepAndResolve() {
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A creature the Vampire damages and kills returns under its controller at end step")
    void returnsDamagedCreatureUnderControlAtEndStep() {
        vampireKillsBearsInCombat();
        advanceToEndStepAndResolve();

        harness.assertOnBattlefield(player1, "Balduvian Bears");
        harness.assertNotInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("A creature the Vampire did not damage does not return when it dies")
    void noReturnForUndamagedCreature() {
        addCreatureReady(player1, new KrovikanVampire());
        Permanent bears = addCreatureReady(player2, new BalduvianBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Balduvian Bears");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("A creature the Vampire damages earlier in the turn returns when it dies later")
    void returnsCreatureDamagedEarlierInTurnWhenItDiesLater() {
        addCreatureReady(player1, new KrovikanVampire());
        Permanent folk = addCreatureReady(player2, new FolkOfThePines());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(player2, TurnStep.END_OF_COMBAT);

        harness.assertOnBattlefield(player2, "Folk of the Pines");
        harness.setHand(player2, List.of(new DarkBanishing()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, folk.getId());
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Folk of the Pines");
        harness.assertNotInGraveyard(player2, "Folk of the Pines");
    }

    @Test
    @DisplayName("Losing control of the Vampire makes its controller sacrifice the returned creature")
    void sacrificesReturnedCreatureOnControlLoss() {
        vampireKillsBearsInCombat();
        advanceToEndStepAndResolve();

        harness.assertOnBattlefield(player1, "Balduvian Bears");

        Permanent vampire = findPermanent(player1, "Krovikan Vampire");
        gd.playerBattlefields.get(player1.getId()).remove(vampire);
        gd.playerBattlefields.get(player2.getId()).add(vampire);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("All creatures the Vampire damages and kills return at end step")
    void returnsAllDamagedCreaturesAtEndStep() {
        addCreatureReady(player1, new KrovikanVampire());
        Permanent firstElves = addCreatureReady(player2, new FyndhornElves());
        Permanent secondElves = addCreatureReady(player2, new FyndhornElves());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstElves.getId(), 1,
                secondElves.getId(), 2));
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Fyndhorn Elves")).isEqualTo(2);
        harness.assertNotInGraveyard(player2, "Fyndhorn Elves");
    }

    @Test
    @DisplayName("The Vampire's current controller controls the return at end step")
    void currentControllerControlsReturnAtEndStep() {
        vampireKillsBearsInCombat();

        Permanent vampire = findPermanent(player1, "Krovikan Vampire");
        gd.playerBattlefields.get(player1.getId()).remove(vampire);
        gd.playerBattlefields.get(player2.getId()).add(vampire);

        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Balduvian Bears");
        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("If the Vampire leaves before end step, it does not trigger")
    void noReturnIfVampireLeavesBeforeEndStep() {
        vampireKillsBearsInCombat();

        Permanent vampire = findPermanent(player1, "Krovikan Vampire");
        harness.setHand(player1, List.of(new WordOfUndoing()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, vampire.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.END_STEP);

        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("A returned creature remains when the Vampire leaves the battlefield")
    void returnedCreatureRemainsWhenVampireLeaves() {
        vampireKillsBearsInCombat();
        advanceToEndStepAndResolve();

        Permanent vampire = findPermanent(player1, "Krovikan Vampire");
        harness.setHand(player1, List.of(new WordOfUndoing()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, vampire.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Balduvian Bears");
        harness.assertNotOnBattlefield(player1, "Krovikan Vampire");
    }

    @Test
    @DisplayName("A creature that leaves and re-enters is not returned after dying")
    void doesNotReturnCreatureAfterItLeavesAndReenters() {
        addCreatureReady(player1, new KrovikanVampire());
        Permanent folk = addCreatureReady(player2, new FolkOfThePines());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(player2, TurnStep.END_OF_COMBAT);

        harness.setHand(player1, List.of(new WordOfUndoing()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, folk.getId());
        harness.passBothPriorities();

        Card bouncedFolk = gd.playerHands.get(player2.getId()).stream()
                .filter(card -> card.getName().equals("Folk of the Pines"))
                .findFirst()
                .orElseThrow();
        harness.setHand(player2, List.of(bouncedFolk));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 5);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent reenteredFolk = findPermanent(player2, "Folk of the Pines");
        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, reenteredFolk.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.END_STEP);

        harness.assertNotOnBattlefield(player1, "Folk of the Pines");
        harness.assertInGraveyard(player2, "Folk of the Pines");
    }

    @Test
    @DisplayName("If the Vampire dies before end step, its damaged creature does not return")
    void noReturnIfVampireDiesBeforeEndStep() {
        addCreatureReady(player1, new KrovikanVampire());
        Permanent firstBears = addCreatureReady(player2, new BalduvianBears());
        Permanent secondBears = addCreatureReady(player2, new BalduvianBears());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBears.getId(), 2,
                secondBears.getId(), 1));
        harness.passUntil(player1, TurnStep.END_STEP);

        harness.assertInGraveyard(player1, "Krovikan Vampire");
        harness.assertInGraveyard(player2, "Balduvian Bears");
        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
    }
}
