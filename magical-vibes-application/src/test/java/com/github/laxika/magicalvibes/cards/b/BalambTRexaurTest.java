package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BalambTRexaur.class, Forest.class})
class BalambTRexaurTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gains 3 life")
    void enteringTheBattlefieldGainsThreeLife() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new BalambTRexaur()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Forestcycling searches for a Forest and discards the card")
    void forestcyclingSearchesForForest() {
        harness.setHand(player1, List.of(new BalambTRexaur()));
        harness.setLibrary(player1, List.of(new Forest(), new BalambTRexaur()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Balamb T-Rexaur");
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Forest");

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
    @Test
    @DisplayName("Forestcycling discards immediately and does not gain life or draw a card")
    void forestcyclingPaysDiscardBeforeResolution() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new BalambTRexaur()));
        harness.setLibrary(player1, List.of(new Forest(), new BalambTRexaur()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Balamb T-Rexaur");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 10);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Balamb T-Rexaur");
        harness.assertNotOnBattlefield(player1, "Balamb T-Rexaur");
        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("Forestcycling can fail to find even when a Forest is available")
    void forestcyclingMayDeclineFindingForest() {
        harness.setHand(player1, List.of(new BalambTRexaur()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
        harness.assertInGraveyard(player1, "Balamb T-Rexaur");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Forestcycling resolves without finding a card when the library has no Forest")
    void forestcyclingWithoutForest() {
        harness.setHand(player1, List.of(new BalambTRexaur()));
        harness.setLibrary(player1, List.of(new BalambTRexaur()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Balamb T-Rexaur");
        harness.assertInGraveyard(player1, "Balamb T-Rexaur");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Forestcycling cannot discard the card when its mana cost cannot be paid")
    void forestcyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new BalambTRexaur()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Balamb T-Rexaur");
        harness.assertNotInGraveyard(player1, "Balamb T-Rexaur");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
