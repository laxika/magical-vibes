package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Convolute;
import com.github.laxika.magicalvibes.cards.l.LoreBroker;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Grozoth.class, Convolute.class, LoreBroker.class})
class GrozothTest extends BaseCardTest {

    @Test
    void entersAndSearchesForAnyNumberOfCardsWithManaValueNine() {
        Grozoth firstMatchingCard = new Grozoth();
        Grozoth secondMatchingCard = new Grozoth();
        Convolute nonMatchingCard = new Convolute();
        harness.setHand(player1, List.of(new Grozoth()));
        harness.setLibrary(player1, List.of(firstMatchingCard, nonMatchingCard, secondMatchingCard));
        harness.addMana(player1, ManaColor.BLUE, 9);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(firstMatchingCard, secondMatchingCard);

        harness.handleCardChosen(player1, 0);
        search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(secondMatchingCard);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstMatchingCard, secondMatchingCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonMatchingCard);
    }

    @Test
    void mayDeclineTheEnterTheBattlefieldSearch() {
        Grozoth matchingCard = new Grozoth();
        harness.setHand(player1, List.of(new Grozoth()));
        harness.setLibrary(player1, List.of(matchingCard));
        harness.addMana(player1, ManaColor.BLUE, 9);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(matchingCard);
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
        Grozoth matchingCard = new Grozoth();
        Convolute nonMatchingCard = new Convolute();
        harness.setHand(player1, List.of(new Grozoth()));
        harness.setLibrary(player1, List.of(matchingCard, nonMatchingCard));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCard);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grozoth");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonMatchingCard);
    }

    @Test
    void transmuteUsesTheSourceManaValueAfterAnotherCardIsDiscardedInResponse() {
        Grozoth matchingCard = new Grozoth();
        Convolute drawnAndDiscardedCard = new Convolute();
        Permanent loreBroker = harness.addToBattlefieldAndReturn(player2, new LoreBroker());
        loreBroker.setSummoningSick(false);

        harness.setHand(player1, List.of(new Grozoth()));
        harness.setLibrary(player1, List.of(drawnAndDiscardedCard, matchingCard));
        harness.setHand(player2, List.of(new Convolute()));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(matchingCard);

        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Grozoth");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
    }

    @Test
    void transmuteCanOnlyBeActivatedAsASorcery() {
        Grozoth grozoth = new Grozoth();
        harness.setHand(player1, List.of(grozoth));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(grozoth);
    }
}
