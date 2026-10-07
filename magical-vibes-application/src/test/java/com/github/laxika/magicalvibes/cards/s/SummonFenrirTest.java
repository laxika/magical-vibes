package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonFenrir.class, Forest.class, GrizzlyBears.class, HillGiant.class, CrawWurm.class})
class SummonFenrirTest extends BaseCardTest {

    @Test
    void chapterISearchesForABasicLandAndPutsItOntoTheBattlefieldTapped() {
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new GrizzlyBears()));
        addSagaWithLore(0);

        advanceToNextChapter();
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest);
        assertThat(search.params().cards()).allMatch(card -> card.hasType(CardType.LAND));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == forest && permanent.isTapped());
    }

    @Test
    void chapterIIPutsAnAdditionalCounterOnTheNextCreatureOnly() {
        addSagaWithLore(1);
        advanceToNextChapter();
        resolveAllTriggers();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        GrizzlyBears firstCard = new GrizzlyBears();
        GrizzlyBears secondCard = new GrizzlyBears();
        harness.setHand(player1, List.of(firstCard, secondCard));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanentByCard(firstCard).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanentByCard(secondCard).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void chapterIIIDrawsWhenYouControlTheGreatestPowerOrAreTied() {
        addSagaWithLore(2);
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToNextChapter();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void chapterIIIDoesNotDrawWhenAnOpponentHasGreaterPower() {
        addSagaWithLore(2);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new CrawWurm());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToNextChapter();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    void chapterICompletesWhenThereIsNoBasicLandToFind() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addSagaWithLore(0);

        advanceToNextChapter();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void chapterIAllowsFailingToFindEvenWhenABasicLandIsAvailable() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        addSagaWithLore(0);
        advanceToNextChapter();
        resolveAllTriggers();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chapterIIDoesNotEmpowerOrConsumeTheGrantForACreatureEnteringWithoutBeingCast() {
        addSagaWithLore(1);
        advanceToNextChapter();
        resolveAllTriggers();

        Permanent uncastCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(uncastCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(permanent -> permanent != uncastCreature)
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(1));
    }

    @Test
    void chapterIICreatesARespondableDelayedTriggerAboveTheCreatureSpell() {
        addSagaWithLore(1);
        advanceToNextChapter();
        resolveAllTriggers();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        resolveAllTriggers();
        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    void chapterIIIDrawsWhenFenrirItselfHasTheGreatestPowerAndThenSacrificesIt() {
        addSagaWithLore(2);
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToNextChapter();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertNotOnBattlefield(player1, "Summon: Fenrir");
        harness.assertInGraveyard(player1, "Summon: Fenrir");
    }

    @Test
    void chapterIIIChecksGreatestPowerAtResolutionRatherThanWhenItTriggers() {
        addSagaWithLore(2);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToNextChapter();
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    void chapterIIIDrawsIfYouGainTheGreatestPowerBeforeResolution() {
        Permanent saga = addSagaWithLore(2);
        harness.addToBattlefield(player2, new CrawWurm());
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToNextChapter();
        saga.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonFenrir());
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private Permanent findPermanentByCard(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst()
                .orElseThrow();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
    }
}
