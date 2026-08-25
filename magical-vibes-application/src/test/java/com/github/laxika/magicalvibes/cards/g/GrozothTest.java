package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Convolute;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InkwellLeviathan;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Grozoth.class, InkwellLeviathan.class, Convolute.class, GrizzlyBears.class})
class GrozothTest extends BaseCardTest {

    @Test
    void entersAndSearchesForAnyNumberOfCardsWithManaValueNine() {
        InkwellLeviathan firstMatchingCard = new InkwellLeviathan();
        InkwellLeviathan secondMatchingCard = new InkwellLeviathan();
        harness.setHand(player1, List.of(new Grozoth()));
        harness.setLibrary(player1, List.of(firstMatchingCard, new Convolute(), secondMatchingCard, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 9);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(firstMatchingCard, secondMatchingCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));
        search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(secondMatchingCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstMatchingCard, secondMatchingCard);
    }

    @Test
    void activatedAbilityRemovesDefenderUntilEndOfTurn() {
        Permanent grozoth = harness.addToBattlefieldAndReturn(player1, new Grozoth());
        assertThat(gqs.hasKeyword(gd, grozoth, Keyword.DEFENDER)).isTrue();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, grozoth, Keyword.DEFENDER)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, grozoth, Keyword.DEFENDER)).isTrue();
    }

    @Test
    void transmuteSearchesForTheSameManaValue() {
        InkwellLeviathan matchingCard = new InkwellLeviathan();
        harness.setHand(player1, List.of(new Grozoth()));
        harness.setLibrary(player1, List.of(matchingCard, new Convolute()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInGraveyard(player1, "Grozoth");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
    }
}
