package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({LunarHatchling.class, Forest.class, GrizzlyBears.class})
class LunarHatchlingTest extends BaseCardTest {

    @Test
    @DisplayName("Basic landcycling searches for a basic land")
    void basicLandcyclingSearchesForBasicLand() {
        harness.setHand(player1, List.of(new LunarHatchling()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lunar Hatchling");
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).singleElement()
                .satisfies(card -> assertThat(card.getName()).isEqualTo("Forest"));

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Escape exiles a land and five other graveyard cards")
    void escapeExilesLandAndOtherGraveyardCards() {
        Card lunarHatchling = new LunarHatchling();
        List<Card> otherCards = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, List.of(lunarHatchling, otherCards.get(0), otherCards.get(1), otherCards.get(2),
                otherCards.get(3), otherCards.get(4)));
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        gs.playFlashbackSpell(gd, player1, 0, null, null, List.of(), List.of(1, 2, 3, 4, 5), null,
                List.of(), null, forest.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(forest.getCard(), otherCards.get(0), otherCards.get(1), otherCards.get(2),
                        otherCards.get(3), otherCards.get(4));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(lunarHatchling.getId()));
    }

    @Test
    @DisplayName("Escape requires a land to exile")
    void escapeRequiresLandToExile() {
        harness.setGraveyard(player1, List.of(new LunarHatchling(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, null, null, List.of(),
                List.of(1, 2, 3, 4, 5), null, List.of(), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void basicLandcyclingDiscardsImmediatelyAndRequiresTwoMana() {
        harness.setHand(player1, List.of(new LunarHatchling()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Lunar Hatchling");
        harness.assertNotInGraveyard(player1, "Lunar Hatchling");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Lunar Hatchling");
        harness.assertInGraveyard(player1, "Lunar Hatchling");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void basicLandcyclingCanFindNothingInAnEmptyLibrary() {
        harness.setHand(player1, List.of(new LunarHatchling()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lunar Hatchling");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void escapeCannotExileItselfOrReuseTheSameGraveyardCard() {
        harness.setGraveyard(player1, List.of(new LunarHatchling(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, null, null, List.of(),
                List.of(0, 1, 2, 3, 4), null, List.of(), null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, null, null, List.of(),
                List.of(1, 1, 2, 3, 4), null, List.of(), null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void normalCastingDoesNotRequireEscapePayments() {
        harness.setHand(player1, List.of(new LunarHatchling()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lunar Hatchling");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void escapeCannotExileAnOpponentsLand() {
        harness.setGraveyard(player1, List.of(new LunarHatchling(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, null, null, List.of(),
                List.of(1, 2, 3, 4, 5), null, List.of(), null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
