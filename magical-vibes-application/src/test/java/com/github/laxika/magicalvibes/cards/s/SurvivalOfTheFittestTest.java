package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SurvivalOfTheFittestTest extends BaseCardTest {

    @Test
    @DisplayName("Activation requires discarding a creature card and does not tap Survival of the Fittest")
    void activationRequiresCreatureDiscardAndDoesNotTapSource() {
        Permanent survival = addSurvival();
        harness.addMana(player1, ManaColor.GREEN, 1);
        Card discardedCreature = new GrizzlyBears();
        harness.setHand(player1, List.of(discardedCreature, new Mountain()));

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);

        assertThat(survival.isTapped()).isFalse();
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId())).contains(discardedCreature);
    }

    @Test
    @DisplayName("Resolving searches for a creature and puts it into hand")
    void resolvingSearchesForCreatureIntoHand() {
        addSurvival();
        harness.addMana(player1, ManaColor.GREEN, 1);
        Card discardedCreature = new GrizzlyBears();
        Card foundCreature = new LlanowarElves();
        List<Card> library = harness.getGameData().playerDecks.get(player1.getId());
        harness.setHand(player1, List.of(discardedCreature));
        library.clear();
        library.addAll(List.of(foundCreature, new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(foundCreature);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();

        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(foundCreature);
    }

    @Test
    @DisplayName("Noncreature cards cannot be found")
    void noncreaturesCannotBeFound() {
        addSurvival();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        List<Card> library = harness.getGameData().playerDecks.get(player1.getId());
        library.clear();
        library.addAll(List.of(new Mountain(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(library).hasSize(2);
    }

    @Test
    @DisplayName("Activation is rejected without a creature card in hand")
    void cannotActivateWithoutCreatureCard() {
        addSurvival();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new Mountain()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a creature card");
    }

    private Permanent addSurvival() {
        Permanent permanent = new Permanent(new SurvivalOfTheFittest());
        harness.getGameData().playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}
