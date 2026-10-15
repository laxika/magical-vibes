package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoaringSandwing.class, Forest.class, Plains.class})
class SoaringSandwingTest extends BaseCardTest {

    @Test
    @DisplayName("When Soaring Sandwing enters, its controller gains 3 life")
    void gainsLifeWhenItEnters() {
        harness.setHand(player1, List.of(new SoaringSandwing()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.setLife(player1, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
    }

    @Test
    @DisplayName("Plainscycling discards the card and offers only Plains cards")
    void plainscyclingDiscardsAndOffersPlains() {
        harness.setHand(player1, List.of(new SoaringSandwing()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLibrary(player1, List.of(new Forest(), new Plains()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Soaring Sandwing");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card.getName().equals("Plains"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Plainscycling pays its cost immediately and puts the chosen Plains in hand")
    void plainscyclingFindsPlainsWithoutGainingLife() {
        Plains plains = new Plains();
        harness.setHand(player1, List.of(new SoaringSandwing()));
        harness.setLibrary(player1, List.of(plains, new Forest()));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Soaring Sandwing");
        harness.assertNotInHand(player1, "Soaring Sandwing");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Plains");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(card -> card.getId().equals(plains.getId()));
        harness.assertNotOnBattlefield(player1, "Plains");
        harness.assertNotOnBattlefield(player1, "Soaring Sandwing");
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Plainscycling may fail to find even when a Plains is available")
    void plainscyclingMayFailToFind() {
        Plains plains = new Plains();
        harness.setHand(player1, List.of(new SoaringSandwing()));
        harness.setLibrary(player1, List.of(plains));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        harness.assertInGraveyard(player1, "Soaring Sandwing");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Plainscycling resolves without a selection when no Plains exists")
    void plainscyclingWithNoMatchingCard() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new SoaringSandwing()));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Soaring Sandwing");
    }

    @Test
    @DisplayName("Plainscycling cannot discard its source without enough mana")
    void plainscyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new SoaringSandwing()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Soaring Sandwing");
        harness.assertNotInGraveyard(player1, "Soaring Sandwing");
        assertThat(gd.stack).isEmpty();
    }
}
