package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BarkformHarvester;
import com.github.laxika.magicalvibes.cards.b.BrambleguardCaptain;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({WhiskervaleForerunner.class, GiantGrowth.class, GrizzlyBears.class, Forest.class, BarkformHarvester.class, BrambleguardCaptain.class, GrafdiggersCage.class})
class WhiskervaleForerunnerTest extends BaseCardTest {

    @Test
    @DisplayName("On your turn, the chosen creature is offered for the battlefield")
    void putsChosenCreatureOntoBattlefieldOnYourTurn() {
        Permanent forerunner = addForerunner();
        GrizzlyBears bears = new GrizzlyBears();
        setTopFive(bears);
        castGrowth(player1, forerunner);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4).doesNotContain(bears);
    }

    @Test
    @DisplayName("Declining the battlefield placement puts the revealed creature into your hand")
    void declinesBattlefieldPlacementToHand() {
        Permanent forerunner = addForerunner();
        GrizzlyBears bears = new GrizzlyBears();
        setTopFive(bears);
        castGrowth(player1, forerunner);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("On another player's turn, the revealed creature goes directly to your hand")
    void putsCreatureIntoHandOnAnotherPlayersTurn() {
        Permanent forerunner = addForerunner();
        GrizzlyBears bears = new GrizzlyBears();
        setTopFive(bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, forerunner.getId());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("The valiant ability triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        Permanent forerunner = addForerunner();
        GrizzlyBears bears = new GrizzlyBears();
        setTopFive(bears);

        castGrowth(player1, forerunner);
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, forerunner.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("An opponent's spell does not use up the first targeting event you control")
    void opponentTargetingDoesNotConsumeValiant() {
        Permanent forerunner = addForerunner();
        GrizzlyBears bears = new GrizzlyBears();
        setTopFive(bears);

        castGrowth(player2, forerunner);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5).contains(bears);

        castGrowth(player1, forerunner);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }

    @Test
    @DisplayName("Creatures above mana value three and noncreatures cannot be selected")
    void noEligibleCardsAreAllReturnedToLibrary() {
        Permanent forerunner = addForerunner();
        WhiskervaleForerunner expensiveCreature = new WhiskervaleForerunner();
        GiantGrowth noncreature = new GiantGrowth();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(expensiveCreature, noncreature, land));

        castGrowth(player1, forerunner);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(expensiveCreature, noncreature, land);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(expensiveCreature, noncreature, land);
    }

    @Test
    @DisplayName("A short library still permits selecting its eligible creature")
    void looksAtAllCardsInShortLibrary() {
        Permanent forerunner = addForerunner();
        GrizzlyBears bears = new GrizzlyBears();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(bears, land));

        castGrowth(player1, forerunner);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("The selected creature is publicly revealed even on another player's turn")
    void revealsCreatureBeforePuttingItIntoHandOnOpponentsTurn() {
        Permanent forerunner = addForerunner();
        GrizzlyBears bears = new GrizzlyBears();
        setTopFive(bears);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        castGrowth(player1, forerunner);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.gameLog).anySatisfy(entry ->
                assertThat(entry.plainText()).contains("reveals", "Grizzly Bears"));
    }

    @Test
    @DisplayName("A creature with mana value exactly three is eligible")
    void acceptsManaValueThreeCreature() {
        Permanent forerunner = addForerunner();
        BarkformHarvester harvester = new BarkformHarvester();
        Forest untouchedTopCard = new Forest();
        harness.setLibrary(player1, List.of(
                harvester, new Forest(), new Forest(), new Forest(), new Forest(), untouchedTopCard));

        castGrowth(player1, forerunner);
        harness.handleMultipleCardsChosen(player1, List.of(harvester.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Barkform Harvester")).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouchedTopCard);
    }

    @Test
    @DisplayName("An ability you control targeting the Forerunner triggers valiant")
    void triggersWhenControlledAbilityTargetsIt() {
        Permanent forerunner = addForerunner();
        addCreatureReady(player1, new BrambleguardCaptain());
        BarkformHarvester harvester = new BarkformHarvester();
        harness.setLibrary(player1, List.of(harvester, new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        harness.handlePermanentChosen(player1, forerunner.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(harvester.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Barkform Harvester")).hasSize(1);
    }

    @Test
    @DisplayName("A revealed creature goes into hand when Grafdigger's Cage prevents entry")
    void putsCreatureIntoHandWhenLibraryEntryIsProhibited() {
        Permanent forerunner = addForerunner();
        harness.addToBattlefield(player2, new GrafdiggersCage());
        BarkformHarvester harvester = new BarkformHarvester();
        harness.setLibrary(player1, List.of(harvester, new Forest()));

        castGrowth(player1, forerunner);
        harness.handleMultipleCardsChosen(player1, List.of(harvester.getId()));
        if (!gd.playerHands.get(player1.getId()).contains(harvester)) {
            harness.handleMayAbilityChosen(player1, true);
        }
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Barkform Harvester")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(harvester);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1).doesNotContain(harvester);
    }

    private Permanent addForerunner() {
        return addCreatureReady(player1, new WhiskervaleForerunner());
    }

    private void setTopFive(GrizzlyBears bears) {
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), bears));
    }

    private void castGrowth(com.github.laxika.magicalvibes.model.Player player, Permanent target) {
        harness.setHand(player, List.of(new GiantGrowth()));
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player, 0, target.getId());
    }
}
