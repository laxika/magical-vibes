package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Fabricate;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({PsychogenicProbe.class, Fabricate.class, Shatter.class})
class PsychogenicProbeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to an opponent who shuffles their library")
    void damagesOpponentWhoShufflesLibrary() {
        harness.addToBattlefield(player1, new PsychogenicProbe());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        LibraryShuffleHelper.shuffleLibrary(gd, player2.getId());

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Deals 2 damage when its controller shuffles their own library")
    void triggersWhenControllerShufflesOwnLibrary() {
        harness.addToBattlefield(player1, new PsychogenicProbe());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        LibraryShuffleHelper.shuffleLibrary(gd, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("A spell shuffling an empty library still triggers damage after the spell resolves")
    void spellShufflingEmptyLibraryTriggers() {
        harness.addToBattlefield(player1, new PsychogenicProbe());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Fabricate(), "{2}{U}");

        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Fabricate");
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Each Probe triggers when an opponent searches and shuffles")
    void multipleProbesTriggerFromOneSpellShuffle() {
        harness.addToBattlefield(player1, new PsychogenicProbe());
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(new PsychogenicProbe()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new Fabricate(), "{2}{U}");

        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.assertInHand(player2, "Psychogenic Probe");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The shuffle trigger still deals damage after Probe is destroyed")
    void triggerResolvesAfterSourceIsDestroyed() {
        harness.addToBattlefield(player1, new PsychogenicProbe());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Fabricate(), "{2}{U}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Psychogenic Probe"));
        harness.assertInGraveyard(player1, "Psychogenic Probe");
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }
}
