package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NishobaBrawler;
import com.github.laxika.magicalvibes.cards.l.LilianaOfTheVeil;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheCrueltyOfGix.class, Forest.class, NishobaBrawler.class, LilianaOfTheVeil.class})
class TheCrueltyOfGixTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I reveals an opponent's hand and discards a chosen creature")
    void chapterIChoosesCreatureToDiscard() {
        Forest land = new Forest();
        NishobaBrawler creature = new NishobaBrawler();
        harness.setHand(player2, List.of(land, creature));
        addSagaWithLore(0);

        triggerNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("Chapter II searches for a card and makes you lose 3 life")
    void chapterIISearchesAndLosesLife() {
        NishobaBrawler creature = new NishobaBrawler();
        harness.setLibrary(player1, List.of(new Forest(), creature));
        harness.setLife(player1, 20);
        addSagaWithLore(1);

        triggerNextChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 1);

        harness.assertInHand(player1, "Nishoba Brawler");
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Chapter III puts a target creature card from a graveyard onto the battlefield")
    void chapterIIIReturnsCreatureUnderYourControl() {
        NishobaBrawler creature = new NishobaBrawler();
        harness.setGraveyard(player2, List.of(creature));
        Permanent saga = addSagaWithLore(2);

        triggerNextChapter();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nishoba Brawler");
        harness.assertNotInGraveyard(player2, "Nishoba Brawler");
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(3);
    }

    @Test
    void chapterICanDiscardPlaneswalker() {
        LilianaOfTheVeil planeswalker = new LilianaOfTheVeil();
        harness.setHand(player2, List.of(new Forest(), planeswalker));
        addSagaWithLore(0);
        triggerNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(1);
        harness.handleCardChosen(player1, 1);
        harness.assertInGraveyard(player2, "Liliana of the Veil");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    void chapterILeavesHandWithoutEligibleCardsUnchanged() {
        Forest land = new Forest();
        harness.setHand(player2, List.of(land));
        addSagaWithLore(0);
        triggerNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void chapterIILosesLifeEvenWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);
        addSagaWithLore(1);
        triggerNextChapter();
        harness.passBothPriorities();
        harness.assertLife(player1, 17);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void chapterIIITargetsOwnCreatureButNotLandAndSacrificesSagaAfterResolution() {
        NishobaBrawler creature = new NishobaBrawler();
        harness.setGraveyard(player1, List.of(new Forest(), creature));
        addSagaWithLore(2);
        triggerNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.assertOnBattlefield(player1, "The Cruelty of Gix");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Nishoba Brawler");
        harness.assertNotOnBattlefield(player1, "The Cruelty of Gix");
        harness.assertInGraveyard(player1, "The Cruelty of Gix");
    }

    @Test
    void readAheadStartsAtChapterTwoAndSkipsDiscard() {
        NishobaBrawler creature = new NishobaBrawler();
        harness.setHand(player2, List.of(creature));
        harness.setLibrary(player1, List.of(new Forest()));
        castSaga();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "2");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertLife(player1, 17);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creature);
    }

    @Test
    void readAheadStartsAtChapterThreeAndSkipsSearchAndLifeLoss() {
        NishobaBrawler creature = new NishobaBrawler();
        harness.setGraveyard(player2, List.of(creature));
        castSaga();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "3");
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Nishoba Brawler");
        harness.assertInGraveyard(player1, "The Cruelty of Gix");
    }

    private void castSaga() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new TheCrueltyOfGix()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheCrueltyOfGix());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
