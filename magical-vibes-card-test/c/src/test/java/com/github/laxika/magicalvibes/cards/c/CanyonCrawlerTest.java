package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CanyonCrawler.class, Swamp.class})
class CanyonCrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Food token when it enters")
    void createsFoodTokenOnEnter() {
        harness.setHand(player1, List.of(new CanyonCrawler()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Swampcycling discards the card and searches for a Swamp")
    void swampcyclingSearchesForSwamp() {
        harness.setHand(player1, List.of(new CanyonCrawler()));
        harness.setLibrary(player1, List.of(new CanyonCrawler(), new Swamp()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Canyon Crawler");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card instanceof Swamp)
                .hasSize(1);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Swamp");
    }

    @Test
    @DisplayName("Food is sacrificed as a cost and gains three life on resolution")
    void foodSacrificesAsCostAndGainsLife() {
        harness.enterBattlefieldAndReturn(player1, new CanyonCrawler());
        harness.passBothPriorities();
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Food"));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, foodIndex, null, null);

        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A tapped Food cannot pay its tap cost")
    void tappedFoodCannotBeActivated() {
        harness.enterBattlefieldAndReturn(player1, new CanyonCrawler());
        harness.passBothPriorities();
        var food = findPermanent(player1, "Food");
        food.tap();
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, foodIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Food");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Swampcycling discards immediately and does not trigger the enters ability")
    void swampcyclingPaysDiscardBeforeResolution() {
        harness.setHand(player1, List.of(new CanyonCrawler()));
        harness.setLibrary(player1, List.of(new Swamp()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Canyon Crawler");
        harness.assertInGraveyard(player1, "Canyon Crawler");
        harness.assertNotInHand(player1, "Swamp");
        harness.assertNotOnBattlefield(player1, "Food");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Swamp");
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Swampcycling resolves without a matching card")
    void swampcyclingWithNoSwamps() {
        harness.setHand(player1, List.of(new CanyonCrawler()));
        harness.setLibrary(player1, List.of(new CanyonCrawler()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Canyon Crawler");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Swampcycling requires two mana before discarding")
    void swampcyclingCannotBeActivatedWithInsufficientMana() {
        harness.setHand(player1, List.of(new CanyonCrawler()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Canyon Crawler");
        harness.assertNotInGraveyard(player1, "Canyon Crawler");
    }
}
