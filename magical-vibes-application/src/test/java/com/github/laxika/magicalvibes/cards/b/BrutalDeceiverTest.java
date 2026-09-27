package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrutalDeceiver.class, Forest.class})
class BrutalDeceiverTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability looks at the top card and leaves it on top")
    void looksAtTopCard() {
        addCreatureReady(player1, new BrutalDeceiver());
        Card topCard = new BrutalDeceiver();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(topCard);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Looking at an empty library does not create a card-choice interaction")
    void lookingAtEmptyLibraryDoesNotPrompt() {
        addCreatureReady(player1, new BrutalDeceiver());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The look ability can be activated more than once each turn")
    void lookAbilityCanBeActivatedMoreThanOnceEachTurn() {
        addCreatureReady(player1, new BrutalDeceiver());
        Card topCard = new BrutalDeceiver();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Revealing a land gives Brutal Deceiver +1/+0 and first strike")
    void landRevealBoostsAndGrantsFirstStrike() {
        Permanent deceiver = addCreatureReady(player1, new BrutalDeceiver());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, deceiver)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, deceiver, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
    }

    @Test
    @DisplayName("Revealing a nonland card leaves Brutal Deceiver unchanged")
    void nonlandRevealDoesNothing() {
        Permanent deceiver = addCreatureReady(player1, new BrutalDeceiver());
        harness.setLibrary(player1, List.of(new BrutalDeceiver()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, deceiver, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(BrutalDeceiver.class);
    }

    @Test
    @DisplayName("Revealing an empty library leaves Brutal Deceiver unchanged")
    void emptyLibraryRevealDoesNothing() {
        Permanent deceiver = addCreatureReady(player1, new BrutalDeceiver());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, deceiver)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, deceiver, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The boost and first strike wear off at end of turn")
    void boostWearsOff() {
        Permanent deceiver = addCreatureReady(player1, new BrutalDeceiver());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, deceiver, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The reveal ability can only be activated once each turn")
    void revealAbilityOnlyOnceEachTurn() {
        addCreatureReady(player1, new BrutalDeceiver());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("The reveal ability can be activated again on a new turn")
    void revealAbilityCanBeActivatedAgainOnNewTurn() {
        Permanent deceiver = addCreatureReady(player1, new BrutalDeceiver());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        advanceToUpkeep(player2);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, deceiver, Keyword.FIRST_STRIKE)).isTrue();
    }
}
