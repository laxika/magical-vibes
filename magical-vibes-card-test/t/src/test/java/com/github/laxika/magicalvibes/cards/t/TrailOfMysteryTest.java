package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AinokTracker;
import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.cards.v.VedalkenOrrery;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrailOfMystery.class, AinokTracker.class, AlpineGrizzly.class, Forest.class})
class TrailOfMysteryTest extends BaseCardTest {

    @Test
    @DisplayName("A face-down creature entering lets you search for a basic land")
    void faceDownCreatureTriggersBasicLandSearch() {
        harness.addToBattlefield(player1, new TrailOfMystery());
        Forest forest = new Forest();
        AlpineGrizzly bears = new AlpineGrizzly();
        harness.setLibrary(player1, List.of(forest, bears));
        harness.setHand(player1, List.of(new AinokTracker()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("A creature turned face up gets +2/+2 until end of turn")
    void turnedFaceUpCreatureGetsBoosted() {
        harness.addToBattlefield(player1, new TrailOfMystery());
        harness.setLibrary(player1, List.of(new AlpineGrizzly()));
        harness.setHand(player1, List.of(new AinokTracker()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent tracker = findPermanent(player1, "Ainok Tracker");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(tracker));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(tracker.isFaceDown()).isFalse();
        assertThat(tracker.getPowerModifier()).isEqualTo(2);
        assertThat(tracker.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    void mayDeclineSearchWithoutMovingLibraryCards() {
        harness.addToBattlefield(player1, new TrailOfMystery());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of(new AinokTracker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed(ThornwoodFalls.class)
    void searchExcludesNonbasicLandsAndMayFailToFind() {
        harness.addToBattlefield(player1, new TrailOfMystery());
        Forest forest = new Forest();
        ThornwoodFalls falls = new ThornwoodFalls();
        harness.setLibrary(player1, List.of(falls, forest));
        harness.setHand(player1, List.of(new AinokTracker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(falls, forest);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void faceUpCreatureEnteringDoesNotTriggerSearch() {
        harness.addToBattlefield(player1, new TrailOfMystery());
        harness.setHand(player1, List.of(new AlpineGrizzly()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Alpine Grizzly");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentsFaceDownCreatureNeitherSearchesNorGetsBoosted() {
        harness.addToBattlefield(player1, new TrailOfMystery());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new AinokTracker()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        Permanent tracker = findPermanent(player2, "Ainok Tracker");
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.turnFaceUp(player2, gd.playerBattlefields.get(player2.getId()).indexOf(tracker));

        assertThat(tracker.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(tracker.getPowerModifier()).isZero();
        assertThat(tracker.getToughnessModifier()).isZero();
    }

    @Test
    void searchStillResolvesIfEnteringCreatureIsTurnedFaceUpInResponse() {
        harness.addToBattlefield(player1, new TrailOfMystery());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of(new AinokTracker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent tracker = findPermanent(player1, "Ainok Tracker");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(tracker));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        assertThat(gqs.getEffectivePower(gd, tracker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, tracker)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void boostExpiresAtCleanup() {
        harness.addToBattlefield(player1, new TrailOfMystery());
        harness.setHand(player1, List.of(new AinokTracker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent tracker = findPermanent(player1, "Ainok Tracker");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(tracker));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, tracker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, tracker)).isEqualTo(5);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, tracker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tracker)).isEqualTo(3);
    }

    @Test
    @CardUsed({SongOfTheDryads.class, VedalkenOrrery.class})
    void noBoostIfTurnedPermanentStopsBeingACreatureBeforeResolution() {
        harness.addToBattlefield(player1, new TrailOfMystery());
        harness.addToBattlefield(player1, new VedalkenOrrery());
        harness.setHand(player1, List.of(new AinokTracker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent tracker = findPermanent(player1, "Ainok Tracker");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(tracker));
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, tracker.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, tracker)).isFalse();
        harness.passBothPriorities();

        assertThat(tracker.getPowerModifier()).isZero();
        assertThat(tracker.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
