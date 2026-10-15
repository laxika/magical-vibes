package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PatternMatcher.class, GrizzlyBears.class, HillGiant.class, Forest.class})
class PatternMatcherTest extends BaseCardTest {

    @Test
    void acceptingMaySearchesForTheOnlyOtherCreatureName() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new HillGiant(), new Forest()));

        castPatternMatcher();
        harness.handleMayAbilityChosen(player1, true);

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).hasSize(1);
        assertThat(offered.getFirst().getName()).isEqualTo("Grizzly Bears");

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void choosingAnotherCreatureUsesThatCreatureName() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new HillGiant(), new Forest()));

        castPatternMatcher();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(bears.getId(), giant.getId());

        harness.handlePermanentChosen(player1, bears.getId());

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).hasSize(1);
        assertThat(offered.getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    void acceptingMayWithoutAnotherCreatureStillSearchesAndShuffles() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        castPatternMatcher();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.playersWhoSearchedLibraryThisTurn).contains(player1.getId());
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    void decliningMayDoesNotSearch() {
        harness.addToBattlefield(player1, new PatternMatcher());
        harness.setLibrary(player1, List.of(new PatternMatcher(), new Forest()));
        castPatternMatcher();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playersWhoSearchedLibraryThisTurn).doesNotContain(player1.getId());
        harness.assertNotInHand(player1, "Pattern Matcher");
    }

    @Test
    void anotherPatternMatcherAllowsFindingAThirdCopy() {
        harness.addToBattlefield(player1, new PatternMatcher());
        harness.setLibrary(player1, List.of(new PatternMatcher(), new Forest()));
        castPatternMatcher();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Pattern Matcher");
        assertThat(gameLogContains("reveals Pattern Matcher")).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void mayFailToFindEvenWhenAMatchingCardExists() {
        harness.addToBattlefield(player1, new PatternMatcher());
        harness.setLibrary(player1, List.of(new PatternMatcher(), new Forest()));
        castPatternMatcher();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);
        harness.assertNotInHand(player1, "Pattern Matcher");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    private void castPatternMatcher() {
        harness.setHand(player1, List.of(new PatternMatcher()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
