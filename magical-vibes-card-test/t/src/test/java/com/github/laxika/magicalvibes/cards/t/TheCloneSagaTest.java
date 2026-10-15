package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.StormCrow;
import com.github.laxika.magicalvibes.cards.s.ScarletSpiderBenReilly;
import com.github.laxika.magicalvibes.cards.s.SchoolDaze;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheCloneSaga.class, Forest.class, GrizzlyBears.class, Island.class, StormCrow.class,
        ScarletSpiderBenReilly.class, SchoolDaze.class})
class TheCloneSagaTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I surveils three")
    void chapterISurveilsThree() {
        addSagaWithLore(0);
        Card first = new Forest();
        Card second = new Island();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, new Island()));

        advanceToNextChapter();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).hasSize(3);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));
    }

    @Test
    @DisplayName("Chapter II copies the next creature spell and removes legendary")
    void chapterIICopiesNextCreatureSpellWithoutLegendary() {
        addSagaWithLore(1);
        advanceToNextChapter();

        Card legendaryCreature = new ScarletSpiderBenReilly();
        harness.setHand(player1, List.of(legendaryCreature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> bears = findPermanents(player1, "Scarlet Spider, Ben Reilly");
        assertThat(bears).hasSize(2);
        assertThat(bears).anyMatch(permanent -> !permanent.getCard().isToken()
                && permanent.getCard().getSupertypes().contains(CardSupertype.LEGENDARY));
        assertThat(bears).anyMatch(permanent -> permanent.getCard().isToken()
                && !permanent.getCard().getSupertypes().contains(CardSupertype.LEGENDARY));
    }

    @Test
    @DisplayName("Chapter III draws for a creature with the chosen name dealing combat damage")
    void chapterIIIDrawsForChosenCreatureName() {
        addSagaWithLore(2);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new StormCrow());
        harness.setLibrary(player1, List.of(new Forest(), new Island()));

        advanceToNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "Grizzly Bears");

        int handBeforeCombat = gd.playerHands.get(player1.getId()).size();
        declareAttackers(List.of(0, 1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBeforeCombat + 1);
    }

    @Test
    void enteringSagaSurveilsAndCanReorderAndPutCardsIntoGraveyard() {
        Card first = new Forest();
        Card second = new Island();
        Card third = new Forest();
        Card fourth = new Island();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new TheCloneSaga()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second, third);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(2, 0), List.of(1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, first, fourth);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second);
        assertThat(findPermanent(player1, "The Clone Saga").getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    void chapterISurveilsAllCardsWhenLibraryHasFewerThanThree() {
        addSagaWithLore(0);
        Card onlyCard = new Island();
        harness.setLibrary(player1, List.of(onlyCard));

        advanceToNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(onlyCard);
    }

    @Test
    void chapterIIIgnoresNoncreatureSpellsAndCopiesOnlyOneCreature() {
        addSagaWithLore(1);
        advanceToNextChapter();
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest(), new Island()));
        harness.setHand(player1, List.of(new SchoolDaze()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castModalInstant(player1, 0, 0, List.of());
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);

        harness.setHand(player1, List.of(new ScarletSpiderBenReilly(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Scarlet Spider, Ben Reilly")).hasSize(2);
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }

    @Test
    void chapterIIIDrawsOnceForEachMatchingCreatureAfterSagaIsSacrificed() {
        addSagaWithLore(2);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest()));

        advanceToNextChapter();
        harness.handleListChoice(player1, "Grizzly Bears");
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "The Clone Saga");
        harness.assertInGraveyard(player1, "The Clone Saga");
        int handBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0, 1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    void chapterIIIRejectsNamesThatAreNotOracleCardNames() {
        addSagaWithLore(2);
        advanceToNextChapter();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Not an Oracle card name"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "The Clone Saga");
    }

    @Test
    void chapterIIIDoesNotDrawOnTheFollowingTurn() {
        addSagaWithLore(2);
        addCreatureReady(player2, new ScarletSpiderBenReilly());
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Forest()));

        advanceToNextChapter();
        harness.handleListChoice(player1, "Scarlet Spider, Ben Reilly");
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.assertLife(player1, 16);
    }

    @Test
    void chapterIICopyPermissionExpiresBeforeTheControllersNextTurn() {
        addSagaWithLore(1);
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest()));
        harness.setLibrary(player2, List.of(new Island(), new Forest()));
        advanceToNextChapter();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Forest");
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ScarletSpiderBenReilly()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Scarlet Spider, Ben Reilly")).hasSize(1);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        harness.addToBattlefield(player1, new TheCloneSaga());
        Permanent saga = findPermanent(player1, "The Clone Saga");
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        resolveAllTriggers();
    }
}
