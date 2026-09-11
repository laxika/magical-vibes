package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Carnophage;
import com.github.laxika.magicalvibes.cards.c.CullingTheWeak;
import com.github.laxika.magicalvibes.cards.w.WoodElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SurvivalOfTheFittest.class, Carnophage.class, WoodElves.class, CullingTheWeak.class})
class SurvivalOfTheFittestTest extends BaseCardTest {

    @Test
    @DisplayName("Activation requires discarding a creature card and does not tap Survival of the Fittest")
    void activationRequiresCreatureDiscardAndDoesNotTapSource() {
        Permanent survival = addSurvival();
        harness.addMana(player1, ManaColor.GREEN, 1);
        Card discardedCreature = new Carnophage();
        harness.setHand(player1, List.of(discardedCreature, new CullingTheWeak()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);

        assertThat(survival.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCreature);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Resolving searches for a creature and puts it into hand")
    void resolvingSearchesForCreatureIntoHand() {
        addSurvival();
        harness.addMana(player1, ManaColor.GREEN, 1);
        Card discardedCreature = new Carnophage();
        Card foundCreature = new WoodElves();
        Card nonCreature = new CullingTheWeak();
        harness.setHand(player1, List.of(discardedCreature));
        harness.setLibrary(player1, List.of(foundCreature, nonCreature));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(foundCreature);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();

        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(foundCreature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonCreature);
    }

    @Test
    @DisplayName("Noncreature cards cannot be found")
    void noncreaturesCannotBeFound() {
        addSurvival();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new Carnophage()));
        Card firstNonCreature = new CullingTheWeak();
        Card secondNonCreature = new CullingTheWeak();
        harness.setLibrary(player1, List.of(firstNonCreature, secondNonCreature));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(firstNonCreature, secondNonCreature);
    }

    @Test
    @DisplayName("Activation is rejected without a creature card in hand")
    void cannotActivateWithoutCreatureCard() {
        addSurvival();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new CullingTheWeak()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard a creature card");
    }

    @Test
    @DisplayName("Activation requires green mana even when a creature can be discarded")
    void cannotActivateWithoutGreenMana() {
        addSurvival();
        Card discardedCreature = new Carnophage();
        harness.setHand(player1, List.of(discardedCreature));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discardedCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(discardedCreature);
    }

    private Permanent addSurvival() {
        return harness.addToBattlefieldAndReturn(player1, new SurvivalOfTheFittest());
    }
}
