package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({
        AangAtTheCrossroads.class,
        AangDestinedSavior.class,
        Forest.class,
        GrizzlyBears.class,
        Island.class,
        Mountain.class,
        SerraAngel.class,
        TurnToFrog.class
})
class AangAtTheCrossroadsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may put a creature with mana value four or less from the top five onto the battlefield")
    void etbPutsEligibleCreatureOntoBattlefield() {
        Card grizzlyBears = new GrizzlyBears();
        Card forest = new Forest();
        Card island = new Island();
        Card mountain = new Mountain();
        Card serraAngel = new SerraAngel();
        harness.setLibrary(player1, List.of(grizzlyBears, forest, island, mountain, serraAngel));

        harness.enterBattlefieldAndReturn(player1, new AangAtTheCrossroads());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(grizzlyBears.getId());
        harness.handleMultipleCardsChosen(player1, List.of(grizzlyBears.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof GrizzlyBears);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(forest, island, mountain, serraAngel);
    }

    @Test
    @DisplayName("ETB can decline putting a creature onto the battlefield")
    void etbCanDeclineCreature() {
        Card grizzlyBears = new GrizzlyBears();
        Card forest = new Forest();
        Card island = new Island();
        Card mountain = new Mountain();
        Card serraAngel = new SerraAngel();
        harness.setLibrary(player1, List.of(grizzlyBears, forest, island, mountain, serraAngel));

        harness.enterBattlefieldAndReturn(player1, new AangAtTheCrossroads());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof GrizzlyBears);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(grizzlyBears, forest, island, mountain, serraAngel);
    }

    @Test
    @DisplayName("Another creature leaving schedules a transform for the next upkeep")
    void anotherCreatureLeavingTransformsAtNextUpkeep() {
        Permanent aang = harness.addToBattlefieldAndReturn(player1, new AangAtTheCrossroads());
        Permanent grizzlyBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        leaveBattlefield(grizzlyBears);
        assertThat(aang.isTransformed()).isFalse();
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();

        assertThat(aang.isTransformed()).isTrue();
        assertThat(aang.getCard()).isInstanceOf(AangDestinedSavior.class);
    }

    @Test
    @DisplayName("The transformed face earthbends a land and gives land creatures vigilance")
    void transformedFaceEarthbendsAtBeginningOfCombat() {
        Permanent aang = harness.addToBattlefieldAndReturn(player1, new AangAtTheCrossroads());
        Permanent grizzlyBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        leaveBattlefield(grizzlyBears);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, land, Keyword.VIGILANCE)).isFalse();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(aang.getCard()).isInstanceOf(AangDestinedSavior.class);
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, land, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void etbWithShortLibraryKeepsUnchosenCards() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(bears, forest));
        harness.enterBattlefieldAndReturn(player1, new AangAtTheCrossroads());
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void etbOnlyLooksAtFiveAndPutsRemainderBelowUntouchedCards() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        Card island = new Island();
        Card mountain = new Mountain();
        Card angel = new SerraAngel();
        Card untouched = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears, forest, island, mountain, angel, untouched));
        harness.enterBattlefieldAndReturn(player1, new AangAtTheCrossroads());
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bears.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrder(forest, island, mountain, angel);
    }

    @Test
    void etbWithNoEligibleCreatureNeedsNoChoice() {
        Card forest = new Forest();
        Card angel = new SerraAngel();
        harness.setLibrary(player1, List.of(forest, angel));
        harness.enterBattlefieldAndReturn(player1, new AangAtTheCrossroads());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, angel);
        harness.assertNotOnBattlefield(player1, "Serra Angel");
    }

    @Test
    void opposingCreatureLeavingDoesNotTransformAang() {
        Permanent aang = harness.addToBattlefieldAndReturn(player1, new AangAtTheCrossroads());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        leaveBattlefield(bears);
        resolveAllTriggers();
        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(aang.isTransformed()).isFalse();
    }

    @Test
    void bouncingAnotherCreatureTransformsAtOpponentsUpkeep() {
        Permanent aang = harness.addToBattlefieldAndReturn(player1, new AangAtTheCrossroads());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bears));
        resolveAllTriggers();
        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(aang.isTransformed()).isTrue();
    }

    @Test
    void multipleDelayedTriggersTransformOnlyOnce() {
        Permanent aang = harness.addToBattlefieldAndReturn(player1, new AangAtTheCrossroads());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        leaveBattlefield(first);
        resolveAllTriggers();
        leaveBattlefield(second);
        resolveAllTriggers();
        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(aang.getCard()).isInstanceOf(AangDestinedSavior.class);
    }

    @Test
    void earthbendedLandReturnsTappedAfterDeath() {
        Permanent land = earthbendForest();
        leaveBattlefield(land);
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Forest");
        assertThat(returned.getId()).isNotEqualTo(land.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void earthbendedLandReturnsTappedAfterExile() {
        Permanent land = earthbendForest();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, land));
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Forest");
        assertThat(returned.getId()).isNotEqualTo(land.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
    }

    @Test
    void earthbendReturnSurvivesLandLosingAbilities() {
        Permanent land = earthbendForest();
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, land.getId());
        resolveAllTriggers();
        leaveBattlefield(land);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
    }

    @Test
    void earthbendDoesNotTriggerDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new AangDestinedSavior());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    @Test
    void earthbendCanOnlyTargetOwnLandsAndDoesNotGrantVigilanceToOtherCreatures() {
        harness.addToBattlefield(player1, new AangDestinedSavior());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingLand, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, land, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void earthbendedLandDoesNotReturnWhenBounced() {
        Permanent land = earthbendForest();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, land));
        resolveAllTriggers();

        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    private Permanent earthbendForest() {
        harness.addToBattlefield(player1, new AangDestinedSavior());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.handlePermanentChosen(player1, land.getId());
        resolveAllTriggers();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        return land;
    }

    private void leaveBattlefield(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
    }
}
